package pervacio.com.customconnectionmeasurer.tasks;

import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import pervacio.com.customconnectionmeasurer.IConnectionTypeChecker;
import pervacio.com.customconnectionmeasurer.TaskException;
import pervacio.com.customconnectionmeasurer.callbacks.DownloadCallback;
import pervacio.com.customconnectionmeasurer.callbacks.TaskCallbacks;
import pervacio.com.customconnectionmeasurer.utils.FileUtils;
import pervacio.com.customconnectionmeasurer.utils.MeasuringUnits;

import static pervacio.com.customconnectionmeasurer.utils.Constants.DOWNLOAD_FILE_NAME;


public class DownloadTask extends AbstractCancelableTask {

    private static final String TAG = "[" + DownloadTask.class.getSimpleName() + "]";

    private String mUrl;
    private DownloadCallback mDownloadCallback;

    public DownloadTask(String url, long duration, MeasuringUnits measuringUnit,
                        IConnectionTypeChecker checker, TaskCallbacks taskCallback) {
        super(duration, measuringUnit, checker);
        mUrl = url;
        mDownloadCallback = taskCallback;
    }

    @Override
    protected void onStart() {
        if (mDownloadCallback != null) {
            mDownloadCallback.onDownloadStart();
        }
    }

    @Override
    void onProgress(float progress) {
        if (mDownloadCallback != null) {
            mDownloadCallback.onDownloadProgress(progress);
        }
    }

    @Override
    void onFinish(float result) {
        if (mDownloadCallback != null) {
            mDownloadCallback.onDownloadFinish(result);
        }
    }

    @Override
    void onError(String message) {
        if (mDownloadCallback != null) {
            mDownloadCallback.onDownloadError(message);
        }
    }

    @Override
    protected float performAction() throws TaskException {
        Log.w(TAG, "performAction()");
        HttpURLConnection connection = null;
        float total = 0;
        try {
            connection = (HttpURLConnection) new URL(mUrl).openConnection();
            connection.connect();
            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                String error = "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
                Log.e(TAG, error);
                throw new TaskException(error);
            }
            // download the file
            total = readBytes(connection.getInputStream(), new FileOutputStream(FileUtils.getFileInDirectory(DOWNLOAD_FILE_NAME)));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            throw new TaskException(e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return total;
    }

}
