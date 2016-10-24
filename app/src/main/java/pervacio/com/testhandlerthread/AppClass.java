package pervacio.com.testhandlerthread;

import android.app.Application;
import android.content.Context;

public class AppClass extends Application {

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
    }

    public static Context getContext(){
        return sContext;
    }

}
