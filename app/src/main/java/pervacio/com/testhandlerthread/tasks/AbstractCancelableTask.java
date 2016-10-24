package pervacio.com.testhandlerthread.tasks;

import android.os.SystemClock;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import pervacio.com.testhandlerthread.callbacks.LifeCycleCallback;

import static pervacio.com.testhandlerthread.utils.Constants.DEFAULT_ERROR_DELAY;

public abstract class AbstractCancelableTask<Result> {

    public static final String TAG = AbstractCancelableTask.class.getSimpleName();

    private final AtomicBoolean mCancelled = new AtomicBoolean();

    private long mDuration;
    private LifeCycleCallback mLifeCycleCallback;

    public AbstractCancelableTask(long duration, LifeCycleCallback lifeCycleCallback) {
        mDuration = duration;
        mLifeCycleCallback = lifeCycleCallback;
    }

    public final boolean isCancelled() {
        return mCancelled.get();
    }

    public final void cancel() {
        mCancelled.set(true);
    }

    public Result start() throws Exception {
        if (mLifeCycleCallback != null) {
            mLifeCycleCallback.onStartRouting();
        }
        onStart();
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(mDuration);
                mCancelled.set(true);
            }
        }).start();
        final Result result = performAction();
        if (mLifeCycleCallback != null) {
            mLifeCycleCallback.onFinishRouting();
        }
        return result;
    }

    public Callable<Result> getCallable() {
        return new Callable<Result>() {
            @Override
            public Result call() throws Exception {
                return start();
            }
        };
    }

    public final static Callable<String> getEmptyCallable(final String message) {
        return new Callable<String>() {
            @Override
            public String call() throws Exception {
                SystemClock.sleep(DEFAULT_ERROR_DELAY);
                return message;
            }
        };
    }

    protected abstract void onStart();

    protected abstract Result performAction();

}
