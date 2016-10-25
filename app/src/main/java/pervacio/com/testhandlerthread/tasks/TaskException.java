package pervacio.com.testhandlerthread.tasks;

public class TaskException extends Exception {

    private String mMessage;

    public TaskException(String message) {
        super(message);
        mMessage = message;
    }

    public String getmMessage() {
        return mMessage;
    }
}
