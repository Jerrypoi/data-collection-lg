package com.example.tommy.dataCollection;

import android.os.Bundle;
import android.support.annotation.Nullable;

public class CollectSegmentSignalActivity extends SignalDetectBaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_segment_signal);
    }

    @Override
    public void onDetect(float[][][] signal) {

    }

    @Override
    public void onStatusChanged(int status) {

    }
}
