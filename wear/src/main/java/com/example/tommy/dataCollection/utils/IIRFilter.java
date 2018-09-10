package com.example.tommy.dataCollection.utils;

import java.util.List;

/**
 * Created by chenlin on 16/01/2018.
 */
public class IIRFilter {
    /**
     * 高通 20Hz/500Hz 参数
     */
    private static float[] hA = {1.0000f, -1.6475f, 0.7009f};
    private static float[] hB = {0.8371f, -1.6742f, 0.8371f};

    /**
     * 高通 40Hz/100Hz 参数
     */
    private static float[] hA40 = {1.0000f, -1.3073f, 0.4918f};
    private static float[] hB40 = {0.6998f, -1.3995f, 0.6998f};

    private static float[] in;
    private static float[] out;
    private static float[] outData;

    public static float[][] filter(List<float[]> signals) {
        float[][] res = new float[signals.size()][];
        for (int i = 0; i < signals.size(); ++i) {
            res[i] =filter(signals.get(i));
        }
        return res;
    }

    public static float[] filter(float[] signal) {
        return filter(signal, hA, hB);
    }

    public static float filter(float[] signal, float[] filteredSignal) {
        return filter(signal, filteredSignal, hA,  hB);
    }

    public static float filter40(float[] signal, float[] filteredSignal) {
        return filter(signal, filteredSignal, hA40,  hB40);
    }

    /**
     * 实时高通滤波, 只计算当前点滤波之后的值
     * @param signal 未滤波的原始信号 (最后一个点为需要滤波的信号)
     * @param filteredSignal 已滤波的信号
     * @return 滤波之后的值
     */
    private static float filter(float[] signal, float[] filteredSignal, float[] a, float[] b) {
        float y = 0.0f;
        for (int i = 0; i < b.length; ++i) {
            y += b[i] * signal[signal.length - i - 1];
        }

        for (int i = 0; i < a.length - 1; ++i) {
            y -= a[i + 1] * filteredSignal[filteredSignal.length - i - 1];
        }

        return y;
    }

    private static float[] filter(float[] signal, float[] a, float[] b) {
        in = new float[b.length];
        out = new float[a.length - 1];
        // 去除高通滤波的前 10 个点
        outData = new float[signal.length - 10];
        for (int i = 0; i < signal.length; i++) {

            System.arraycopy(in, 0, in, 1, in.length - 1);  //in[1]=in[0],in[2]=in[1]...
            in[0] = signal[i];

            //calculate y based on a and b coefficients
            //and in and out.
            float y = 0.0f;
            for (int j = 0; j < b.length; j++) {
                y += b[j] * in[j];
            }

            for (int j = 0; j < a.length - 1; j++) {
                y -= a[j + 1] * out[j];
            }

            //shift the out array
            System.arraycopy(out, 0, out, 1, out.length - 1);
            out[0] = y;
            //Log.d("radio", "i" + i + "length" + outData.length);
            if (i >= 10) {
                outData[i - 10] = y;
            }
        }
        return outData;
    }

}
