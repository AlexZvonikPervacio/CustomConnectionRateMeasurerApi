package pervacio.com.customconnectionmeasurer.utils;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Constants {

    public static final int DEFAULT_ERROR_DELAY = 1000;

    public static final String HOST = "http://2.testdebit.info";
    public static final String DOWNLOAD_FILE_NAME = "100Mo.dat";

    public static final String DOWNLOAD_URL = HOST + "/fichiers/" + DOWNLOAD_FILE_NAME;
    public static final String DOWNLOAD_URL2 = "http://mirror.internode.on.net/pub/speed/SpeedTest_128MB.dat";
    public static final String DOWNLOAD_URL3 = "http://test.talia.net/dl/50mb.pak";
    public static final String DOWNLOAD_URL4 = "http://speedo.eltele.no/speedtest/random4000x4000.jpg";

    public static final String UPLOAD_URL = HOST + "/";
    public static final int UPLOAD_FILE_SIZE = 100_000_000; //upload 100Mo file size.
    public static final String UPLOAD_FILE_PREF = "100_UL_Mo";
    public static final String UPLOAD_FILE_SUF = "dat";
    public static final String UPLOAD_FILE_NAME = UPLOAD_FILE_PREF + "." + UPLOAD_FILE_SUF;


    public static final String CHARSET = "UTF-8";

    public static final int ACTION_START = 8000;
    public static final int ACTION_STOP = 8001;
    public static final int DEFAULT_MEASUREMENT_DURATION = 10_000;

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

}
