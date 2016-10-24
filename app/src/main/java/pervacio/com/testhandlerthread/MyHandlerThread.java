package pervacio.com.testhandlerthread;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

import pervacio.com.testhandlerthread.utils.Constants;

import static pervacio.com.testhandlerthread.utils.Constants.DOWNLOAD_URL;

public class MyHandlerThread extends HandlerThread {

    private static final String TAG = MyHandlerThread.class.getSimpleName();

    private Handler mUiHandler;

    private Router mRouter;

    private AfterInit mAfterInit;

    public MyHandlerThread(AfterInit afterInit) {
        super(MyHandlerThread.class.getSimpleName(), Process.THREAD_PRIORITY_BACKGROUND);
        mAfterInit = afterInit;
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();

        mRouter = new Router();

        Handler.Callback callback = new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                Log.w(TAG, message.arg1 + " got a message in " + Thread.currentThread() + " " + message.toString());
                switch (message.arg1) {
                    case Constants.ACTION_START:
                        Log.w(TAG, "ACTION_START");
                        final Callable<String> callable = new Callable<String>() {
                            @Override
                            public String call(){
                                return mRouter.load(DOWNLOAD_URL);
                            }
                        };
                        ExecutorService executor = Executors.newFixedThreadPool(1);
                        final Future<String> submit = executor.submit(callable);
                        executor.shutdown();

                        FutureTask<String> future = new FutureTask<String>(callable);
                        new Thread(future).start(); // FutureTask implements Runnable
                        break;
                    case Constants.ACTION_STOP:
                        Log.w(TAG, "ACTION_STOP");
                        mRouter.cancel();
                        Log.w(TAG, "mRouter.getTotal() = " + mRouter.getTotal());
                        break;
                }
                return false;
            }
        };
        Handler mHtHandler = new Handler(getLooper(), callback);
        try {
            Thread.sleep(1001);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mUiHandler = mAfterInit.swapHandlers(mHtHandler);
        mUiHandler.sendEmptyMessage(222);
    }

    public interface AfterInit {
        Handler swapHandlers(Handler workerHandler);
    }

    private static class Router {

        public static final String TAG = "[" + Router.class.getSimpleName() + "]";

        private final AtomicBoolean mCancelled = new AtomicBoolean();

        private int mFileLength;
        private int mTotal;

        public int getTotal() {
            return mTotal;
        }

        private String load(String string) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(string);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                boolean support = connection.getHeaderField("Accept-Ranges").equals("bytes");
                Log.w("DownloadTask", "Partial content retrieval support = " + (support ? "Yes" : "No"));
                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    String error = "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                    Log.e(TAG, error);
                    return error;
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                mFileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream("/sdcard/file_name.extension");

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        Log.e(TAG, "isCancelled");
                        return null;
                    }
                    total += count;
                    // publishing the progress....
//                    if (fileLength > 0) // only if total length is known
//                        publishProgress((int) (total * 100 / fileLength));
                    publishProgress((int) total);
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                Log.e(TAG, "catch " +  e.toString());
                return e.toString();
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
            Log.e(TAG,  "return null");
            return null;
        }

        public final void cancel() {
            mCancelled.set(true);
        }

        public final boolean isCancelled() {
            return mCancelled.get();
        }

        private void publishProgress(int total) {
            mTotal = total;
            Log.w(TAG, "[publishProgress()]: progress - " + total);
        }

    }


}
