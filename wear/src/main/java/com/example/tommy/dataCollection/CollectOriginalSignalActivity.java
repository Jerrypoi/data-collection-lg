package com.example.tommy.dataCollection;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.example.tommy.dataCollection.services.DataCollectService;
import com.example.tommy.dataCollection.utils.FileTransfer;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;

import java.util.ArrayList;

public class CollectOriginalSignalActivity extends WearableActivity
    implements DataCollectService.OnReceiveListener {
    Button startBtn;

    ArrayList<Short>[] data;

    DataCollectService service;
    FileTransfer fileTransfer;
    boolean start = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_original_signal);

        startBtn = (Button) findViewById(R.id.startBtn);

        service = DataCollectService.getInstance();

        fileTransfer = new FileTransfer(this);

        data = new ArrayList[6];
        for (int i = 0; i < 6; ++i) {
            data[i] = new ArrayList<>();
        }
    }

    @Override
    public void onReceive(short[] res) {
        if (start) {
            if (data[0].size() == 0) {
                showText("开始采集");
            }
            for (int i = 0; i < 6; ++i) {
                data[i].add(res[i]);
            }
        }
    }

    public void clickStart(View view) {
        start = !start;
        startBtn.setText(start ? "Stop" : "Start");

        if (start) {
            service.addEventListener(this);
            for (int i = 0; i < 6; ++i) {
                data[i].clear();
            }
        } else {
            service.removeEventListener(this);
        }
    }

    public void clickSave(View view) {
        showText("未实现");
    }

    public void clickSend(final View view) {
        boolean res = fileTransfer.send(data, new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                showText(dataItemResult.getStatus().isSuccess() ? "发送完成" : "发送失败!");
                view.setEnabled(true);
            }
        });
        if (res) {
            view.setEnabled(false);
        }
        showText(res ? "发送中请稍后..." : "发送失败,请检查网络情况");
    }

    private void showText(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CollectOriginalSignalActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
