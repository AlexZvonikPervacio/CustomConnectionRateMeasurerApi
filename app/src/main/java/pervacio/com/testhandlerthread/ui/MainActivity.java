package pervacio.com.testhandlerthread.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import pervacio.com.testhandlerthread.R;
import pervacio.com.testhandlerthread.TestRouter;
import pervacio.com.testhandlerthread.callbacks.TaskCallbacks;
import pervacio.com.testhandlerthread.utils.Constants;

public class MainActivity extends AppCompatActivity implements TaskCallbacks {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TestRouter mTestRouter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTestRouter = new TestRouter.Builder(this)
                .setNetworkType(Constants.WIFI)
                .setDuration(5_000)
                .setDownload(this)
//                .setUpload(this)
                .create();
        mTestRouter.startRouting();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mTestRouter.addTaskAndStart(Constants.DOWNLOAD, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mTestRouter.finishRouting();
    }

    public void start(View view) {
        mTestRouter.start();
    }

    public void stop(View view) {

    }

    @Override
    public void onStartRouting() {
        Log.w(TAG, "[onStartRouting]");
    }

    @Override
    public void onDownloadStart() {
        Log.w(TAG, "[onDownloadStart]");
    }

    @Override
    public void onDownloadProgress(float progress) {
        Log.w(TAG, "[onDownloadProgress] : progress = " + progress);
    }

    @Override
    public void onDownloadFinish(float result) {
        Log.w(TAG, "[onDownloadFinish] : result = " + result);
    }

    @Override
    public void onDownloadError(String message) {
        Log.w(TAG, "[onDownloadError] : message = " + message);
    }

    @Override
    public void onUploadStart() {
        Log.w(TAG, "[onUploadStart]");
    }

    @Override
    public void onUploadProgress(float progress) {
        Log.w(TAG, "[onUploadProgress] : progress = " + progress);
    }

    @Override
    public void onUploadFinish(float result) {
        Log.w(TAG, "[onUploadFinish] : result = " + result);
    }

    @Override
    public void onUploadError(String message) {
        Log.w(TAG, "[onUploadError] : message = " + message);
    }

    @Override
    public void onFinishRouting() {
        Log.w(TAG, "[onFinishRouting]");
    }

    @Override
    public void onHorribleError(String message) {
        Log.e(TAG, "[onHorribleError] : message = " + message);
        throw new RuntimeException(message);
    }


}
