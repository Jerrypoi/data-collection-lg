package com.example.tommy.dataCollection.utils;

import android.app.Activity;
import android.text.method.ScrollingMovementMethod;
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

        logBoard.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    public static void log(final String tag, final String msg) {
        Log.i(tag, msg);
        if (logBoard != null) {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 记录当前是否滚动到底部，如果当前在底部则再添加文本后自动滚动到底部
                    boolean shouldScrollToBottom = isScrollToBottom();
                    logBoard.append(String.format("\n[%s]: (%s) %s", new SimpleDateFormat("hh:mm:ss").format(new Date()), tag, msg));
                    if (shouldScrollToBottom) {
                        logBoard.setScrollY(Math.max(0, getContentHeight() - logBoard.getHeight()));
                    }
                }
            });
        }
    }

    /**
     * 判断 logBoard 是否滚动到底部
     */
    private static boolean isScrollToBottom() {
        int scrollY = logBoard.getScrollY();
        int boardHeight = logBoard.getHeight();
        int contentHeight = getContentHeight();

        return scrollY + boardHeight >= contentHeight;
    }

    /**
     * 获取 logBoard 的实际文字占用高度
     * @return
     */
    private static int getContentHeight() {
        return logBoard.getLineCount() * logBoard.getLineHeight();
    }
}
