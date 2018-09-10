package com.example.tommy.dataCollection.utils;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2018/1/16.
 */

public class FileTransfer implements DataApi.DataListener {
    private static final String TAG = "FileTransfer";
    private static final String[] DATA_KEY_MAP = {"xAcceData", "yAcceData", "zAcceData", "xGyroData", "yGyroData", "zGyroData"};

    private static GoogleApiClient mGoogleApiClient;

    private String savePath;

    public FileTransfer(Context ctx, String filePath) {
        LogUtil.log(TAG, "初始化...");
        this.savePath = filePath;

        initClient(ctx);
    }

    private void initClient(Context ctx) {
        mGoogleApiClient = new GoogleApiClient.Builder(ctx)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Wearable.DataApi.addListener(mGoogleApiClient, FileTransfer.this);
                        LogUtil.log(TAG, "连接成功!");
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        LogUtil.log(TAG, "连接挂起! cause = " + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        LogUtil.log(TAG, "建立连接失败: " + result);
                    }
                })
                .addApi(Wearable.API)
                .build();
        connect();
    }

    public void connect(){
        mGoogleApiClient.connect();
    }

    public void disconnect(){
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        LogUtil.log(TAG, "Received a data map");
        for (DataEvent event : dataEventBuffer) {
            Log.i(TAG, "onDataChanged: event type = " + event.getType());
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = event.getDataItem();
                Log.i(TAG, "DataPath = " + item.getUri().getPath() + "\n");

                //如果需要拓展的话，需要重写onDataChanged
                if ("/send_file".equals(item.getUri().getPath())) {
                    DataMap dm = DataMapItem.fromDataItem(item).getDataMap();
                    saveAsset(dm);
                    LogUtil.log(TAG, "save file finished");
                }
            }
        }
    }

    private void saveAsset(DataMap dataMap) {
        File dir = new File(savePath);
        for (int i = 0; i < DATA_KEY_MAP.length;i++) {
            int cnt = 0;
            String data = dataMap.getString(DATA_KEY_MAP[i]);
            if (data != null) {
                String fileName = DATA_KEY_MAP[i] + cnt;
                File file = new File(dir,fileName);
                while (file.exists()) {
                    fileName = DATA_KEY_MAP[i] + cnt++;
                    file = new File(dir,fileName);
                }
                saveDataToDisk(data, file);
            }
        }
    }

    private void saveDataToDisk(final String data, final File file) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(data.getBytes());
                    fos.flush();
                    fos.close();

                    LogUtil.log(TAG, String.format("file %s saved.", file.getName()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
