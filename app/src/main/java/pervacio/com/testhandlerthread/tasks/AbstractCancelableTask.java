package pervacio.com.testhandlerthread.tasks;

import android.os.SystemClock;
import android.support.annotation.CheckResult;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import pervacio.com.testhandlerthread.IConnectionTypeChecker;
import pervacio.com.testhandlerthread.utils.MeasuringUnits;

public abstract class AbstractCancelableTask {

    public static final String TAG = AbstractCancelableTask.class.getSimpleName();
    public static final int CHUNK_SIZE = 4 * 1024;

    private final AtomicBoolean mCancelled = new AtomicBoolean();

    private long mDuration;
    private MeasuringUnits mMeasuringUnit;
    private IConnectionTypeChecker mChecker;
    //TODO get rid of or change architecture
//    private LifeCycleCallback mLifeCycleCallback;

    public AbstractCancelableTask(long duration, MeasuringUnits measuringUnit, IConnectionTypeChecker checker) {
        mDuration = duration;
        mMeasuringUnit = measuringUnit;
        mChecker = checker;
    }

    public final boolean isCancelled() {
        return mCancelled.get();
    }

    public final void cancel() {
        mCancelled.set(true);
    }

    public float startAction() {
        initBeforeStart();
        String message = mChecker.check();
        if (message != null) {
            onError(message);
            return 0f;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(mDuration);
                mCancelled.set(true);
            }
        }).start();
        onStart();
        float result = 0f;
        try {
            result = performAction();
            onFinish(result);
        } catch (TaskException e) {
            onError(e.getmMessage());
        }
        return result;
    }

    @CheckResult
    protected float readBytes(InputStream inputStream, OutputStream outputStream) throws TaskException {

        long totalBytes = 0;
        byte[] buffer = new byte[CHUNK_SIZE];
        long startTime = System.currentTimeMillis();

        try {
            int bytesRead;
            int counter = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                if (isCancelled()) {
                    inputStream.close();
                    final float rate = mMeasuringUnit.convertBytes(totalBytes, System.currentTimeMillis() - startTime);
                    Log.e("readBytes", "rate = " + rate + ", totalBytes = " + totalBytes);
                    return rate;
                }
                totalBytes += bytesRead;
                outputStream.write(buffer, 0, bytesRead);
                if (counter % 50 == 0) {
                    onProgress(totalBytes);
                }
                counter++;
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            throw new TaskException(e.getMessage());
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ignored) {
            }
        }
        final float rate = mMeasuringUnit.convertBytes(totalBytes, System.currentTimeMillis() - startTime);
        Log.e("readBytes", "rate = " + rate + ", totalBytes = " + totalBytes);
        return rate;
    }

    protected void initBeforeStart() {
    }

    abstract float performAction() throws TaskException;

    abstract void onStart();

    abstract void onProgress(float result);

    abstract void onFinish(float result);

    abstract void onError(String message);

    public Callable<Float> getCallable() {
        return new Callable<Float>() {
            @Override
            public Float call() throws Exception {
                return startAction();
            }
        };
    }

    public static Callable<String> getEmptyCallable(final String message) {
        return new Callable<String>() {
            @Override
            public String call() throws Exception {
                return message;
            }
        };
    }

}
