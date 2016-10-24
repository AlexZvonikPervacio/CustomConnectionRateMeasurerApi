package pervacio.com.testhandlerthread.utils;

import android.os.SystemClock;

import java.util.concurrent.Future;

import pervacio.com.testhandlerthread.callbacks.LifeCycleCallback;

public class FutureWaiter implements Runnable{

    public static final int INTERVAL = 50;
    public static final int MAX_ATTEMPTS = 20;

    private Future<Float> lastTaskFuture;
    private long waitTime;
    private LifeCycleCallback last;

    public FutureWaiter(Future<Float> lastTaskFuture, long waitTime, LifeCycleCallback last) {
        this.lastTaskFuture = lastTaskFuture;
        this.waitTime = waitTime;
        this.last = last;
    }

    @Override
    public void run() {
        SystemClock.sleep(waitTime);
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            if (lastTaskFuture.isDone()) {
                last.onFinishRouting();
                return;
            }
            SystemClock.sleep(INTERVAL);
        }
        last.onHorribleError("HorribleError. Somehow threads didn't stop");
    }
}
