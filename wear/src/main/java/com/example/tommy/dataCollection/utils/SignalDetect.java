package com.example.tommy.dataCollection.utils;

import com.example.tommy.dataCollection.objects.Data;
import com.example.tommy.dataCollection.services.DataCollectService;

/**
 * Created by chenlin on 27/03/2018.
 */
public class SignalDetect implements DataCollectService.OnReceiveListener {

    public static final int STATUS_WAITING = 0;
    public static final int STATUS_KNOCK = 1;
    public static final int STATUS_FOUND = 2;

    /**
     * 信号大小的阈值，大于 threshold 才视为信号
     */
    private static final float THRESHOLD = 50f;

    /**
     * 当信号的大小 < noiseThreshold 时认为属于噪音
     */
    private static final float NOISE_THRESHOLD = 30f;

    /**
     *  检测的信号中要求一个信号前需要有一段平滑的噪音
     *  该参数指定这段噪音的长度
     */
    private static final int SMOOTH_LEN = 30;

    private int signalLen; // 最小信号长度
    private int maxSignalLen; // 最大信号长度
    private int beforeLen; // 找到信号后向前取 beforeLen 个点作为信号的起点
    private int returnSignalLen; // 返回数据的长度 (包含 cache)
    private int cacheSize; // 缓冲区大小

    private Data[][] data;
    private Data detectData;
    private Data filteredDetectData;
    private Data filteredDetectData40;

    private int lessCount;

    /**
     * 信号起始位置
     */
    private int begin = 0;

    /**
     * 信号检测状态
     * 状态0 为初始状态，需要检测到一段平缓的信号后进入 状态1
     * 状态1 为未进入信号，此时需要检测信号的起点，检测到起点后进入 状态2
     * 状态2 为检测信号终点
     * 状态3 为读取信号末端的缓冲区数据
     */
    private int state = STATUS_WAITING;

    private DataCollectService service;
    private OnDetectSignalListener listener;

    public SignalDetect(int fs, OnDetectSignalListener listener) {
        super();
        this.signalLen = 100;
        this.maxSignalLen = 130;
        this.beforeLen = 40;
        this.returnSignalLen = 100;
        this.cacheSize = 15;
        this.listener = listener;

        // 记录信号长度的 4 倍数据
        data = new Data[2][3];
        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 3; ++j) {
                data[i][j] = new Data(signalLen * 4);
            }
        }
        detectData = data[0][2];
        filteredDetectData = new Data(signalLen * 4);
        filteredDetectData40 = new Data(signalLen * 4);

        service = DataCollectService.getInstance();
    }

    public void startDetect() {
        lessCount = 0;
        state = 0;
        service.addEventListener(this);
    }

    public void stopDetect() {
        service.removeEventListener(this);
    }

    public int getCacheSize() {
        return cacheSize;
    }

    @Override
    public void onReceive(short[] res) {
        data[0][0].push(res[0]);
        data[0][1].push(res[1]);
        data[0][2].push(res[2]);
        data[1][0].push(res[3]);
        data[1][1].push(res[4]);
        data[1][2].push(res[5]);

        filteredDetectData.push(IIRFilter.filter(detectData.getBefore(10), filteredDetectData.getBefore(10)));
        filteredDetectData40.push(IIRFilter.filter40(detectData.getBefore(10), filteredDetectData40.getBefore(10)));
        detect();
    }

    /**
     * 检测信号
     */
    private void detect() {
        if (detectData.getLength() < signalLen) {
            return;
        }
        if (state == 0) {
            readSmoothSignal();
        } else if (state == 1) {
            waitSignal();
        } else if (state == 2) {
            detectSignalEnd();
        } else if (state == 3) {
            readSignalEnd();
        }
    }

    /**
     * 读取一个信号前的平滑噪音
     * 读取到后将状态修改为 状态1
     *
     * 平滑噪音需要满足：
     * 1. 连续 smoothLen 个信号值小于 noiseThreshold
     * 2. 连续 smoothLen 个信号方差小于 smoothVariance
     */
    private void readSmoothSignal() {
        if (Math.abs(filteredDetectData40.get()) < NOISE_THRESHOLD) {
            lessCount = lessCount + 1;
            if (lessCount > SMOOTH_LEN) {
                state = 1;
                listener.onStatusChanged(STATUS_KNOCK);
                lessCount = 0;
            }
        } else {
            lessCount = 0;
        }
    }

    /**
     * 等待检测到一个信号的开头
     * 检测到信号开头后跳转到 状态2
     *
     * 信号开头需要满足：
     * 1. 当前信号值的大小大于阈值
     * 2. 接下来一帧的数据的方差 >= signalVariance
     */
    private void waitSignal() {
        if (Math.abs(filteredDetectData40.get()) > THRESHOLD) {
            begin = filteredDetectData40.getLength() - beforeLen;
            state = 2;
            listener.onStatusChanged(STATUS_FOUND);
        }
    }

    /**
     * 等待检测一个信号的结束
     */
    private void detectSignalEnd() {
        if (Math.abs(filteredDetectData40.get()) < NOISE_THRESHOLD) {
            ++lessCount;
            if (lessCount >= SMOOTH_LEN) {
                lessCount = 0;
                if (checkSignalLen() && checkSignalSTN()) {
                    state = 3;
                } else {
                    state = 1;
                    listener.onStatusChanged(STATUS_KNOCK);
                }
            }
        } else {
            lessCount = 0;
        }
    }

    /**
     * 读取信号最后的噪音作为缓冲区
     */
    private void readSignalEnd() {
        if (getSignalLen() > returnSignalLen) {
            listener.onDetect(getAllSignalData());
            state = 1;
            listener.onStatusChanged(STATUS_KNOCK);
        }
    }

    private int getSignalLen() {
        return filteredDetectData40.getLength() - begin;
    }

    /**
     * 判断当前获取的信号的信号长度是否合法
     */
    private boolean checkSignalLen() {
        int len = getSignalLen();
        return len >= signalLen - 30 && len <= maxSignalLen;
    }

    /**
     * 检测信号的信噪比
     */
    private boolean checkSignalSTN() {
        float[] data20 = filteredDetectData.get(begin, filteredDetectData.getLength() - begin);
        float[] data40 = filteredDetectData40.get(begin, filteredDetectData40.getLength() - begin);
        float energyNoise = 0, energySignal = 0;
        for (int i = 0; i < cacheSize; ++i) {
            energyNoise += data20[i] * data20[i];
        }
        for (int i = cacheSize; i < data20.length; ++i) {
            energySignal += data20[i] * data20[i];
        }
        if (energySignal / energyNoise <= 10) {
            return false;
        }
        for (int i = 0; i < cacheSize; ++i) {
            energyNoise += data40[i] * data40[i];
        }
        for (int i = cacheSize; i < data40.length; ++i) {
            energySignal += data40[i] * data40[i];
        }
        return energySignal / energyNoise > 20;
    }

    private float[][][] getAllSignalData() {
        float[][][] res = new float[data.length][][];
        for (int s = 0; s < data.length; ++s) {
            res[s] = new float[3][];
            for (int i = 0; i < 3; ++i) {
                res[s][i] = data[s][i].get(begin + 10, returnSignalLen);
            }
        }
        return res;
    }

    public interface OnDetectSignalListener {
        void onDetect(float[][][] signal);
        void onStatusChanged(int status);
    }

}
