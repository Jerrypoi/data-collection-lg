package com.example.tommy.dataCollection.objects;

/**
 * Created by chenlin on 26/03/2018.
 */
public class Data {
    private float[] data = null;
    private int size;
    private int p; // 当前已记录数据的长度

    public Data(int size) {
        this.size = size;
        reset();
    }

    public int push(float value) {
        data[p % size] = value;
        ++p;
        return p;
    }

    public void reset() {
        p = 0;

        if (data == null) {
            data = new float[size];
        } else {
            for (int i = 0; i < data.length; ++i) {
                data[i] = 0;
            }
        }
    }

    /**
     * 获取数组最后一个元素
     */
    public float get() {
        return get(p - 1);
    }

    public float get(int pos) {
        pos %= size;
        return data[pos];
    }

    public float[] get(int pos, int len) {

        float[] tmp = new float[len];
        for (int i = pos; i < pos + len; ++i) {
            tmp[i-pos] = i < 0 ? get(0) : get(i);
        }
        return tmp;
    }

    /**
     * 获取前
     * @param len 个值
     */
    public float[] getBefore(int len) {
        return get(p - len, len);
    }

    public int getSize() {
        return size;
    }

    public int getLength() {
        return p;
    }
}
