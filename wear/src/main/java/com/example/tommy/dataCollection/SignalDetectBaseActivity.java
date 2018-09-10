package com.example.tommy.dataCollection;

import android.support.wearable.activity.WearableActivity;
import android.view.MotionEvent;
import com.example.tommy.dataCollection.utils.SignalDetect;

/**
 * Created by chenlin on 09/04/2018.
 */
public abstract class SignalDetectBaseActivity extends WearableActivity implements SignalDetect.OnDetectSignalListener {
    private static final String TAG = "BaseActivity";
    private static int delay = 600; // 手指离开屏幕后 delay(ms) 再将 touchScreen 设置为 false;

    private SignalDetect signalDetect;
    private boolean isDetectingSignal;
    private int touchScreen;

    public SignalDetectBaseActivity() {
        super();
        touchScreen = 0;
        isDetectingSignal = false;
        signalDetect = new SignalDetect(500, new TouchInterceptor());
    }

    public void startDetectSignal() {
        isDetectingSignal = true;
        signalDetect.startDetect();
    }

    public void stopDetectSignal() {
        isDetectingSignal = false;
        signalDetect.stopDetect();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                synchronized (this) {
                    ++touchScreen;
                }
                break;
            case MotionEvent.ACTION_UP:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(delay);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        synchronized (SignalDetectBaseActivity.this) {
                            --touchScreen;
                        }
                    }
                }).start();
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    public int getCacheSize() {
        return signalDetect.getCacheSize();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isDetectingSignal) {
            signalDetect.startDetect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isDetectingSignal) {
            signalDetect.stopDetect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isDetectingSignal) {
            signalDetect.stopDetect();
        }
    }

    public class TouchInterceptor implements SignalDetect.OnDetectSignalListener {
        @Override
        public void onDetect(float[][][] signal) {
            if (touchScreen != 0) {
                return;
            }
            SignalDetectBaseActivity.this.onDetect(signal);
        }

        @Override
        public void onStatusChanged(int status) {
            if (touchScreen != 0) {
                return;
            }
            SignalDetectBaseActivity.this.onStatusChanged(status);
        }
    }
}
