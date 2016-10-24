package pervacio.com.testhandlerthread.tasks;

import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import pervacio.com.testhandlerthread.callbacks.DownloadCallback;
import pervacio.com.testhandlerthread.callbacks.TaskCallbacks;
import pervacio.com.testhandlerthread.utils.RandomGen;

public class DownloadTask extends AbstractCancelableTask<Float> {

    private static final String TAG = "[" + DownloadTask.class.getSimpleName() + "]";

    private String mUrl;
    private int mFileLength;
    private DownloadCallback mDownloadCallback;

    public DownloadTask(String url, long duration, TaskCallbacks taskCallback) {
        super(duration, taskCallback);
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
    protected Float performAction() {
        Log.w(TAG, "performAction()");
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        long total = 0;
        try {
            URL url = new URL(mUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                String error = "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
                if (mDownloadCallback != null) {
                    mDownloadCallback.onDownloadError(error);
                }
                Log.e(TAG, error);
                return null;
            }
            // this will be useful to display download percentage
            // might be -1: server did not report the length
            mFileLength = connection.getContentLength();
            // download the file
            input = connection.getInputStream();
            output = new FileOutputStream(RandomGen.getFile());

            byte data[] = new byte[4096];
            int count;
            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
                if (isCancelled()) {
                    input.close();
                    Log.d(TAG, "isCancelled");
                    if (mDownloadCallback != null) {
                        mDownloadCallback.onDownloadFinish(total);
                    }
                    return (float) total;
                }
                total += count;
                if (mDownloadCallback != null) {
                    mDownloadCallback.onDownloadProgress(total);
                }
                // publishing the progress....
//                    if (fileLength > 0) // only if total length is known
//                        publishProgress((int) (total * 100 / fileLength));
//                publishProgress((int) total);
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            if (mDownloadCallback != null) {
                mDownloadCallback.onDownloadError(e.getMessage());
            }
            Log.e(TAG, e.getMessage());
            return null;
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }
        if (mDownloadCallback != null) {
            mDownloadCallback.onDownloadFinish(total);
        }
        return (float) total;
    }

}
