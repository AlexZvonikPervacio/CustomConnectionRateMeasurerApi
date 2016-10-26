package pervacio.com.customconnectionmeasurer.callbacks;

public interface UploadCallback {

    void onUploadStart();

    void onUploadProgress(float progress);

    void onUploadFinish(float result);

    void onUploadError(String message);
}
