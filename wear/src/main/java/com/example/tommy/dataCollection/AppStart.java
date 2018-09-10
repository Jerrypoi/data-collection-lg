package com.example.tommy.dataCollection;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class AppStart extends Activity {
    private static final String TAG = "APP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_start);
    }

    public void startSegmentSignalActivity(View view) {
        startActivity(CollectSegmentSignalActivity.class);
    }

    public void startOriginalSignalActivity(View view) {
        startActivity(CollectOriginalSignalActivity.class);
    }

    private void startActivity(Class<?> cls) {
        startActivity(new Intent(this, cls));
    }

}
