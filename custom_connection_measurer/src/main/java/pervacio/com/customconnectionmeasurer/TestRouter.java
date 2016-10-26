package pervacio.com.customconnectionmeasurer;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import pervacio.com.customconnectionmeasurer.callbacks.LifeCycleCallback;
import pervacio.com.customconnectionmeasurer.callbacks.TaskCallbacks;
import pervacio.com.customconnectionmeasurer.tasks.AbstractCancelableTask;
import pervacio.com.customconnectionmeasurer.tasks.DownloadTask;
import pervacio.com.customconnectionmeasurer.tasks.UploadTask;
import pervacio.com.customconnectionmeasurer.utils.CommonUtils;
import pervacio.com.customconnectionmeasurer.utils.Constants;
import pervacio.com.customconnectionmeasurer.utils.FutureWaiter;
import pervacio.com.customconnectionmeasurer.utils.MeasuringUnits;

import static pervacio.com.customconnectionmeasurer.utils.Constants.CHARSET;
import static pervacio.com.customconnectionmeasurer.utils.Constants.DOWNLOAD;
import static pervacio.com.customconnectionmeasurer.utils.Constants.DOWNLOAD_URL;
import static pervacio.com.customconnectionmeasurer.utils.Constants.UPLOAD;
import static pervacio.com.customconnectionmeasurer.utils.Constants.UPLOAD_URL;

public class TestRouter {

    private ExecutorService mExecutor;
    private Context mContext;

    @Constants.NetworkType
    private int mNetworkType;
    private int mDuration;
    private MeasuringUnits mMeasuringUnit;
    private SparseArray<TaskCallbacks> mCallbackMap;
    private List<AbstractCancelableTask> mPrevTasks;

    private TestRouter(@Constants.NetworkType int networkType, int duration, MeasuringUnits measuringUnit,
                       SparseArray<TaskCallbacks> callbackMap, Context context) {
        mNetworkType = networkType;
        mDuration = duration;
        mMeasuringUnit = measuringUnit;
        mCallbackMap = callbackMap;
        mContext = context;
        mPrevTasks = new ArrayList<>();
    }

    public void start() {
        executeAndClear(mDuration);
    }

    public void start(long maxDuration) {
        executeAndClear(maxDuration);
    }

    public void addTaskAndStart(@Constants.MeasureTaskType int type, TaskCallbacks callbacks) {
        mCallbackMap.append(type, callbacks);
        Log.w("executeAndClear", "mCallbackMap.size = " + mCallbackMap.size());
        executeAndClear(mDuration);
    }

    public void addTaskAndStart(@Constants.MeasureTaskType int type, TaskCallbacks callbacks, long maxDuration) {
        mCallbackMap.append(type, callbacks);
        executeAndClear(maxDuration);
    }

    public void addTask(@Constants.MeasureTaskType int type, TaskCallbacks callbacks) {
        mCallbackMap.append(type, callbacks);
    }

    private void executeAndClear(final long maxDuration) {
        if (mCallbackMap.size() == 0) {
            throw new RuntimeException("No actions to execute");
        }
        //First task start and last task completes check
        mPrevTasks.clear();
        final LifeCycleCallback firstCallback = mCallbackMap.get(mCallbackMap.keyAt(0));
        final int lastIndex = mCallbackMap.size() - 1;
        final LifeCycleCallback lastCallback = mCallbackMap.get(mCallbackMap.keyAt(lastIndex));
        Log.w("executeAndClear", "mCallbackMap.size = " + mCallbackMap.size());
        Log.w("executeAndClear", "lastIndex = " + lastIndex + " lastCallback = null " + (lastCallback == null));
        Future<Float> lastTaskFuture = null;
        firstCallback.onStartRouting();
        //
        final IConnectionTypeChecker connectionChecker = CommonUtils.getConnectionChecker(mNetworkType, mContext);
        AbstractCancelableTask prevTask;
        for (int i = 0; i < mCallbackMap.size(); i++) {
            int key = mCallbackMap.keyAt(i);
            switch (key) {
                case DOWNLOAD:
                    prevTask = new DownloadTask(DOWNLOAD_URL, maxDuration, mMeasuringUnit,
                            connectionChecker, mCallbackMap.get(key));

                    lastTaskFuture = mExecutor.submit(prevTask.getCallable());
                    mPrevTasks.add(prevTask);
                    break;
                case UPLOAD:
                    prevTask = new UploadTask(UPLOAD_URL, CHARSET, maxDuration, mMeasuringUnit,
                            connectionChecker, mCallbackMap.get(key));
                    mPrevTasks.add(prevTask);
                    lastTaskFuture = mExecutor.submit(prevTask.getCallable());
                    break;
            }
        }
        waitForLastTaskCompleted(mCallbackMap.size() * maxDuration, lastTaskFuture, lastCallback);
        mCallbackMap.clear();
    }

    public void cancelAllTasks() {
        for (AbstractCancelableTask cancelableTask : mPrevTasks) {
            cancelableTask.cancel();
        }
    }

    private void waitForLastTaskCompleted(long maxDuration, Future<Float> lastFuture, LifeCycleCallback lastCallback) {
        new FutureWaiter(maxDuration * mCallbackMap.size(), lastFuture, lastCallback).start();
    }

    public void startRouting() {
        if (mExecutor == null || mExecutor.isShutdown() || mExecutor.isTerminated()) {
            mExecutor = Executors.newSingleThreadExecutor();
        }
    }

    public void finishRouting() {
        if (mExecutor != null) {
            mExecutor.shutdown();
            mExecutor = null;
        }
    }

    public static class Builder {

        @Constants.NetworkType
        private int mNetworkType;
        private int mDuration;
        private MeasuringUnits mMeasuringUnit;
        private SparseArray<TaskCallbacks> mCallbackMap;
        private Context mContext;

        public Builder(Context context) {
            mContext = context;
            mNetworkType = Constants.WIFI;
            mDuration = Constants.DEFAULT_MEASUREMENT_DURATION;
            mMeasuringUnit = MeasuringUnits.MB_S;
            mCallbackMap = new SparseArray<>(2);
        }

        public Builder setNetworkType(@Constants.NetworkType int networkType) {
            mNetworkType = networkType;
            return this;
        }

        public Builder setDuration(int duration) {
            mDuration = duration;
            return this;
        }

        public Builder setMeasuringUnit(MeasuringUnits measuringUnit) {
            mMeasuringUnit = measuringUnit;
            return this;
        }

        public Builder setDownload(TaskCallbacks download) {
            mCallbackMap.append(DOWNLOAD, download);
            return this;
        }

        public Builder setUpload(TaskCallbacks upload) {
            mCallbackMap.append(UPLOAD, upload);
            return this;
        }

        public TestRouter create() {
            return new TestRouter(mNetworkType, mDuration, mMeasuringUnit, mCallbackMap, mContext);
        }

    }

}
