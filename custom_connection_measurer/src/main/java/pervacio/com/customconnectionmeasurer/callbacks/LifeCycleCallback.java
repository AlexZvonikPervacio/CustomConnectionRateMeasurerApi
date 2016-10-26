package pervacio.com.customconnectionmeasurer.callbacks;

public interface LifeCycleCallback {

    void onStartRouting();

    void onFinishRouting();

    void onHorribleError(String message);
}
