package pervacio.com.testhandlerthread.callbacks;

public interface DownloadCallback {

    void onDownloadStart();

    void onDownloadProgress(float progress);

    void onDownloadFinish(float result);

    void onDownloadError(String message);
}
