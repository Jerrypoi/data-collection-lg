package com.example.tommy.dataCollection.utils;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.*;

import java.util.ArrayList;

public class FileTransfer {
    private static final String TAG = "FileTransfer";
    private static final String[] DATA_KEY_MAP = {"xAcceData", "yAcceData", "zAcceData", "xGyroData", "yGyroData", "zGyroData"};

    private static GoogleApiClient mGoogleApiClient;

    public FileTransfer(Context ctx) {
        Log.i(TAG, "初始化...");

        initClient(ctx);
    }

    private void initClient(Context ctx) {
        mGoogleApiClient = new GoogleApiClient.Builder(ctx)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.i(TAG, "连接成功!");
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.i(TAG, "连接挂起! cause = " + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.i(TAG, "建立连接失败: " + result);
                    }
                })
                .addApi(Wearable.API)
                .build();
    }

    public boolean send(ArrayList<Short>[] data, ResultCallback<DataApi.DataItemResult> callback) {
        PutDataMapRequest dataMap = PutDataMapRequest.create("/send_file");
        DataMap dm = dataMap.getDataMap();
        try {
            for (int i = 0 ; i < data.length; ++i) {
                Asset asset = Asset.createFromBytes(array2String(data[i]).getBytes());
                dm.putAsset(DATA_KEY_MAP[i], asset);
            }

            PutDataRequest request = dataMap.asPutDataRequest();
            if (!mGoogleApiClient.isConnected()) {
                Log.i(TAG, "establish connection");
                mGoogleApiClient.connect();
            }

            PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, request);
            pendingResult.setResultCallback(callback);
            return true;
        } catch (Exception e) {
            Log.i(TAG, "Error:" + e.toString());
        }
        return false;
    }

    private String array2String(ArrayList<Short> array) {
        StringBuilder sb = new StringBuilder();
        int len = array.size();
        if (len == 0) {
            return "";
        }
        sb.append(String.valueOf(array.get(0)));
        for (int i = 1; i < len; ++i) {
            sb.append("," + String.valueOf(array.get(i)));
        }
        return sb.toString();
    }
}
