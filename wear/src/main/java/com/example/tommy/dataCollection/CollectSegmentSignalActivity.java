package com.example.tommy.dataCollection;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.WindowManager;

public class CollectSegmentSignalActivity extends SignalDetectBaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_segment_signal);

        // keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onDetect(float[][][] signal) {

    }

    @Override
    public void onStatusChanged(int status) {

    }
}
