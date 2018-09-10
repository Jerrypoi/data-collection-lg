package com.example.tommy.dataCollection;

import android.app.Application;
import com.example.tommy.dataCollection.services.DataCollectService;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        DataCollectService.getInstance().start();
    }
}
