package pervacio.com.customconnectionmeasurer.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import pervacio.com.customconnectionmeasurer.IConnectionTypeChecker;

import static pervacio.com.customconnectionmeasurer.tasks.UploadTask.TAG;
import static pervacio.com.customconnectionmeasurer.utils.Constants.MOBILE;
import static pervacio.com.customconnectionmeasurer.utils.Constants.NONE;
import static pervacio.com.customconnectionmeasurer.utils.Constants.WIFI;

public class CommonUtils {

    public static IConnectionTypeChecker getConnectionChecker(@Constants.NetworkType final int networkTime, final Context context) {
        return new IConnectionTypeChecker() {
            @Override
            public String check() {
                return getConnectionErrorMessage(networkTime, context);
            }
        };
    }

    @WorkerThread
    public static String getConnectionErrorMessage(@Constants.NetworkType int networkTime, Context context) {
        String messageResId = null;
        if (!CommonUtils.hasInternetAccess(context)) {
            messageResId = "No internet connection. Please, turn it on";
        } else if (WIFI == networkTime && CommonUtils.typeConnection(context) != WIFI) {
            messageResId = "WiFi is not connected. Please, retry";
        } else if (MOBILE == networkTime && CommonUtils.typeConnection(context) != MOBILE) {
            messageResId = "Mobile internet is not connected. Please, retry";
        }
        return messageResId;
    }

    @Constants.NetworkType
    public static int typeConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected()) {
                    return WIFI;
                }
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected()) {
                    return MOBILE;
                }
        }
        return NONE;
    }

    @WorkerThread
    public static boolean hasInternetAccess(Context context) {
        if (isNetworkAvailable(context)) {
            try {
                HttpURLConnection url = (HttpURLConnection) new URL("http://clients3.google.com/generate_204")
                        .openConnection();
                url.setRequestProperty("User-Agent", "Android");
                url.setRequestProperty("Connection", "close");
                url.setConnectTimeout(1500);
                url.connect();
                return (url.getResponseCode() == 204 && url.getContentLength() == 0);
            } catch (IOException e) {
                Log.e(TAG, "Error checking internet connection", e);
            }
        }
        return false;
    }

    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}

