package pervacio.com.testhandlerthread.callbacks;

public interface LifeCycleCallback {

    void onStartRouting();

    void onFinishRouting();

    void onHorribleError(String message);
}
