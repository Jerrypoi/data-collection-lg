package com.example.tommy.dataCollection.utils;

import android.os.StrictMode;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;

/**
 * Created by Tommy on 2018/1/17.
 */

public class FTPUtil {
    private static final int PORT = 21;

    private FTPClient ftpClient = null;

    private String ipAddr;
    private String username;
    private String password;
    private String savePath;

    private static FTPUtil ftpUtilsInstance = null;

    /*
     * 得到类对象实例（因为只能有一个这样的类对象，所以用单例模式）
     */
    public static FTPUtil getInstance() {
        if (ftpUtilsInstance == null)
        {
            ftpUtilsInstance = new FTPUtil();
        }
        return ftpUtilsInstance;
    }

    private FTPUtil()
    {
        ftpClient = new FTPClient();
    }

    /**
     * 登录 FTP 服务器
     * @param ipAddr   服务器 ip 地址
     * @param username FTP 用户名
     * @param password FTP 密码
     */
    public boolean login(String ipAddr, String username, String password, String savePath) {
        this.ipAddr = ipAddr;
        this.username = username;
        this.password = password;
        this.savePath = savePath;

        if (ftpClient.isConnected()) {
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        int reply;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            //1.要连接的FTP服务器Url,Port
            ftpClient.connect(ipAddr, PORT);

            //2.登陆FTP服务器
            ftpClient.login(username, password);

            //3.看返回的值是不是230，如果是，表示登陆成功
            reply = ftpClient.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply)) {
                //断开
                ftpClient.disconnect();
                return false;
            }

            return true;
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 上传文件
     * @param filePath    要上传文件所在SDCard的路径
     * @param dir         要上传文件的远程文件夹
     * @param fileName    要上传的文件的文件名(如：Sim唯一标识码)
     */
    public boolean uploadFile(String filePath, String dir, String fileName) {

        if (!ftpClient.isConnected()) {
            if (!login(ipAddr, username, password, savePath)) {
                return false;
            }
        }

        try {
            //设置存储路径
            ftpClient.makeDirectory(savePath);
            ftpClient.changeWorkingDirectory(savePath);

            //设置上传文件需要的一些基本信息
            ftpClient.setBufferSize(1024);
            ftpClient.setControlEncoding("UTF-8");
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            //文件上传吧～
            FileInputStream fileInputStream = new FileInputStream(filePath);
            ftpClient.makeDirectory(dir);
            ftpClient.changeWorkingDirectory(dir);
            ftpClient.storeFile(fileName, fileInputStream);

            //关闭文件流
            fileInputStream.close();
            return true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }
}