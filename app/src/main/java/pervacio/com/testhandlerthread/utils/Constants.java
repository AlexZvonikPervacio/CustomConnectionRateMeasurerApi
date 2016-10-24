package pervacio.com.testhandlerthread.utils;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Constants {

    public static final int DEFAULT_ERROR_DELAY = 1000;

    public static final String DOWNLOAD_URL = "https://2.testdebit.info/fichiers/50Mo.dat";
    public static final String UPLOAD_URL = "http://2.testdebit.info";

    public static final String CHARSET = "UTF-8";

    public static final int ACTION_START = 8000;
    public static final int ACTION_STOP = 8001;

    @IntDef({ACTION_START, ACTION_STOP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ACTIONS {
    }


    public static final int DOWNLOAD = 1;
    public static final int UPLOAD = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DOWNLOAD, UPLOAD})
    public @interface MeasureTaskType {

    }

    public static final int NONE = 0;
    public static final int WIFI = 1;
    public static final int MOBILE = 2;

    @IntDef({NONE, WIFI, MOBILE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface NetworkType {
    }

    enum Status {
        START,
        START_DOWNLOAD, START_UPLOAD,
        STOP_DOWNLOAD, STOP_UPLOAD,
        STOP
    }


}
