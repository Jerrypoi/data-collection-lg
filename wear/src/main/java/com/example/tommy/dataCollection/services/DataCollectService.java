package com.example.tommy.dataCollection.services;

import android.util.Log;

import java.io.*;
import java.util.HashSet;

/**
 * Created by chenlin on 27/03/2018.
 */
public class DataCollectService {
    private static final String TAG = "DataCollectService";
    private static final String COMMAND_TAG = "Command";

    private static DataCollectService instance;

    private Process p;
    private DataInputStream is;
    private DataOutputStream os;

    private HashSet<OnReceiveListener> listeners = new HashSet<>();

    public DataCollectService() {
        startThread();
    }

    public static DataCollectService getInstance() {
        if (instance == null) {
            synchronized (DataCollectService.class) {
                if (instance == null) {
                    instance = new DataCollectService();
                }
            }
        }
        return instance;
    }

    public void addEventListener(OnReceiveListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeEventListener(OnReceiveListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public void stop() {
        exitShell();
    }

    public void start() {
        startThread();
    }

    private void startThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    p = Runtime.getRuntime().exec("su");
                    is = new DataInputStream(p.getInputStream());
                    os = new DataOutputStream(p.getOutputStream());

                    exec(os, "cd /sys/class/misc/fastacc_mpu/device", "切换路径...");
                    exec(os, "echo 0 > pwr", "关闭电源...");
                    exec(os, "cat pwr", "检测是否关闭成功...");

                    // 读取电源打印信息
                    byte b = is.readByte();
                    if (b != '0') {
                        Log.i(TAG, "run: 关闭电源失败！请重试。");
                        return;
                    }

                    Log.i(TAG, "run: 关闭电源成功！");
                    is.readByte(); // 读取一个 \r

                    File file = new File("/sys/class/misc/fastacc_mpu/device/fifo");
                    DataInputStream fis = new DataInputStream(new FileInputStream(file));
                    Log.i(TAG, "run: 清除已有数据");
                    byte[] bytes = new byte[168000];
                    fis.read(bytes);

                    exec(os, "echo 1 > pwr", "开启电源...");
                    Thread.sleep(100);

                    boolean powerOn = true;
                    while (powerOn) {
                        short[] res = new short[6];
                        for (int j = 0; j < 6; ++j) {
                            try {
                                res[j] = fis.readShort();
                            } catch (EOFException e) {
                                powerOn = false;
                            }
                        }
                        if (powerOn) {
                            for (OnReceiveListener listener : listeners) {
                                listener.onReceive(res);
                            }
                        }
                    }

                    exitShell();

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void exec(DataOutputStream os, String cmd, String cmdDesc) {
        Log.i(COMMAND_TAG, cmd + ": " + cmdDesc);
        try {
            os.writeBytes(cmd);
            os.writeByte('\n');
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exitShell() {
        Log.i(TAG, "exitShell: 关闭传感器 & 退出 shell");
        exec(os, "echo 0 > pwr", "关闭传感器...");
        exec(os, "exit", "退出 shell");
        p = null;
    }

    public interface OnReceiveListener {
        void onReceive(short[] res);
    }
}
