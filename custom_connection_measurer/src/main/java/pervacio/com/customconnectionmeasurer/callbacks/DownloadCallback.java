package pervacio.com.customconnectionmeasurer.callbacks;

public interface DownloadCallback {

    void onDownloadStart();

    void onDownloadProgress(float progress);

    void onDownloadFinish(float result);

    void onDownloadError(String message);
}
