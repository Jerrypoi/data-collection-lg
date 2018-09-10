package com.example.tommy.dataCollection.utils;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtil {
    private static TextView logBoard = null;
    private static Activity context;

    public static void initContext(Activity context, TextView logBoard) {
        LogUtil.context = context;
        LogUtil.logBoard = logBoard;
    }

    public static void log(final String tag, final String msg) {
        Log.i(tag, msg);
        if (logBoard != null) {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    logBoard.append(String.format("\n[%s]: (%s) %s", new SimpleDateFormat("hh:mm:ss").format(new Date()), tag, msg));
                }
            });
        }
    }
}
