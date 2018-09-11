package com.example.tommy.dataCollection;

import android.content.SharedPreferences;
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
    final String BASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SensorRecords/";
    final String FILE_PATH = BASE_PATH + new SimpleDateFormat("yyyy-MM-dd").format(new Date());

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
        initEditTextValue();

        fileTransfer = new FileTransfer(this, FILE_PATH);
        fileTransfer.connect();

        LogUtil.log(TAG, "本次采集数据将存放在: " + FILE_PATH);

        File dataFolder = new File(FILE_PATH);
        if (!dataFolder.exists()) {
            boolean res = dataFolder.mkdirs();
            LogUtil.log(TAG, "创建文件夹" + (res ? "成功" : "失败"));
        }
    }

    private void initEditTextValue() {
        SharedPreferences sp = getSharedPreferences("Main", MODE_PRIVATE);
        ipEt.setText(sp.getString("ip", "172.31.73.46"));
        userEt.setText(sp.getString("user", "admin"));
        passwordEt.setText(sp.getString("password", "654321"));
        pathEt.setText(sp.getString("path", "/data/"));
    }

    public void ftpLogin(View view) {
        String ip = ipEt.getText().toString();
        String user = userEt.getText().toString();
        String password = passwordEt.getText().toString();
        String path = pathEt.getText().toString();

        boolean res = FTPUtil.getInstance().login(ip, user, password, path);

        if (res) {
            // 登录成功则保存当前信息到手机上
            SharedPreferences sp = getSharedPreferences("Main", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("ip", ip);
            editor.putString("user", user);
            editor.putString("password", password);
            editor.putString("path", path);
        }

        LogUtil.log("FTP", res ? "登录成功!" : "登录失败, 请检查网络设置.");
    }

    public void onSend(View view) {
        LogUtil.log("Main", "开始发送数据");
        sendFiles(new File(FILE_PATH));
    }

    public void onSendAll(View view) {
        File[] files = new File(BASE_PATH).listFiles();
        if (files == null) {
            LogUtil.log("Main", "文件夹为空,没有需要发送的数据");
            return;
        }

        LogUtil.log("Main", "开始发送数据");
        for (File file : files) {
            if (file.isDirectory()) {
                sendFiles(file);
            }
        }
    }

    /**
     * 发送 dir 下的所有文件
     * @param dir
     */
    public void sendFiles(final File dir) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File[] files = dir.listFiles();

                if (files == null) {
                    LogUtil.log("Main", "发送失败~文件夹为空");
                    return;
                }

                for (File file :files) {
                    if (file.isFile()) {
                        final String filename = file.getName();

                        String remotePath = formatRemotePath(pathEt.getText().toString());

                        final boolean uploadFileRes = FTPUtil.getInstance().uploadFile(
                                file.getAbsolutePath(), remotePath + dir.getName(), filename);
                        LogUtil.log("FTP", "send file " + filename + (uploadFileRes ? " --success" : " --failed"));
                    }
                }
            }
        }).start();
    }

    /**
     * 规范远程路径，保证路径名前后都有 '/'
     * 如: 将 'data' 转换为  '/data/'
     * @param path
     */
    private String formatRemotePath(String path) {
        if (path.length() == 0) {
            path = "/";
        }
        if (path.charAt(0) != '/') {
            path = '/' + path;
        }
        if (path.charAt(path.length() - 1) != '/') {
            path = path + '/';
        }

        return path;
    }

    public void deleteToday(View view) {
        LogUtil.log("Main", "删除今天的数据...");
        deleteFolder(new File(FILE_PATH), false);
        LogUtil.log("Main", "删除完毕");
    }

    public void deleteAll(View view) {
        File[] files = new File(FILE_PATH).listFiles();
        if (files == null) {
            LogUtil.log("Main", "没有需要删除的数据");
            return;
        }

        LogUtil.log("Main", "删除所有数据...");
        deleteFolder(new File(BASE_PATH), false);
        // 删除后需要重新创建今天的文件夹
        new File(FILE_PATH).mkdirs();

        LogUtil.log("Main", "删除完毕");
    }

    /**
     * 递归删除文件夹下的所有文件和文件夹
     * @param dir
     * @param shouldDeleteFolder 是否需要将 dir 删除
     */
    private void deleteFolder(File dir, boolean shouldDeleteFolder) {
        if (dir.isFile()) {
            dir.delete();
            return;
        }

        File[] contents = dir.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (f.isDirectory()) {
                    deleteFolder(f, true);
                }
                f.delete();
            }
        }
        if (shouldDeleteFolder) {
            dir.delete();
            LogUtil.log("Main", String.format("已删除 %s", dir.getAbsolutePath()));
        }
    }

    public void clearLogs(View view) {
        logBoard.setText("Logs:");
    }
}
