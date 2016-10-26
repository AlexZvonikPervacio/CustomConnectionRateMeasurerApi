package pervacio.com.testhandlerthread.utils;

import android.os.SystemClock;

import java.util.concurrent.Future;

import pervacio.com.testhandlerthread.callbacks.LifeCycleCallback;

public class FutureWaiter extends Thread {

    public static final int INTERVAL = 50;
    public static final int MAX_ATTEMPTS = 20;

    private long mWaitTime;
    private Future<Float> mLastTaskFuture;
    private LifeCycleCallback mLastCallback;

    public FutureWaiter(long waitTime, Future<Float> lastFuture, LifeCycleCallback lastCallback) {
        mWaitTime = waitTime;
        mLastTaskFuture = lastFuture;
        mLastCallback = lastCallback;
    }

    @Override
    public void run() {
        SystemClock.sleep(mWaitTime);
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            if (mLastTaskFuture.isDone()) {
                mLastCallback.onFinishRouting();
                return;
            }
            SystemClock.sleep(INTERVAL);
        }
        mLastCallback.onHorribleError("HorribleError. Somehow threads didn't stop");
    }
}
