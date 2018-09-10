package com.example.tommy.dataCollection;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.tommy.dataCollection.utils.FTPUtil;
import com.example.tommy.dataCollection.utils.FileTransfer;
import com.example.tommy.dataCollection.utils.LogUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity{
    final String TAG = "Main";
    final String FILE_PATH =
            String.format(Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/SensorRecords/" +
                    new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

    TextView logBoard;
    FileTransfer fileTransfer;
    EditText ipEt, userEt, passwordEt, pathEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logBoard = (TextView) findViewById(R.id.LogWindow);
        LogUtil.initContext(this, logBoard);

        ipEt = (EditText) findViewById(R.id.ipEt);
        userEt = (EditText) findViewById(R.id.userEt);
        passwordEt = (EditText) findViewById(R.id.passwordEt);
        pathEt = (EditText) findViewById(R.id.pathEt);

        fileTransfer = new FileTransfer(this, FILE_PATH);
        fileTransfer.connect();

        LogUtil.log(TAG, "本次采集数据将存放在: " + FILE_PATH);

        File dataFolder = new File(FILE_PATH);
        if (!dataFolder.exists()) {
            boolean res = dataFolder.mkdirs();
            LogUtil.log(TAG, "创建文件夹" + (res ? "成功" : "失败"));
        }
    }

    public void ftpLogin(View view) {
        boolean res = FTPUtil.getInstance().login(
                ipEt.getText().toString(),
                userEt.getText().toString(),
                passwordEt.getText().toString(),
                pathEt.getText().toString()
        );
        LogUtil.log("FTP", res ? "登录成功!" : "登录失败, 请检查网络设置.");
    }

    public void onSend(View view) {
        LogUtil.log("Main", "开始发送数据");
        new Thread(new Runnable() {
            @Override
            public void run() {
                File directory = new File(FILE_PATH);
                File[] files = directory.listFiles();

                if (files == null) {
                    LogUtil.log("Main", "发送失败~文件夹为空");
                    return;
                }

                for (File file :files) {
                    final String filename = file.getName();
                    final boolean uploadFileRes = FTPUtil.getInstance().uploadFile(file.getAbsolutePath(), filename);
                    LogUtil.log("FTP", "send file" + filename + (uploadFileRes ? " --success" : " --failed"));
                }
            }
        }).start();
    }
}
