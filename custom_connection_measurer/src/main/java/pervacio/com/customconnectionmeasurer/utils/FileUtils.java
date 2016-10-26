package pervacio.com.customconnectionmeasurer.utils;

import android.os.Environment;

import java.io.File;

public class FileUtils {

    public static final String DIRECTORY_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();

    private static FileUtils instance;

    private String mCacheDirPath;
    private static File fileInDirect;

    public FileUtils() {
    }

    public static File getFileInDirectory(String fileName) {
        return new File(DIRECTORY_PATH + "/" + fileName);
    }

    public void init(String cacheDirPath) {
        mCacheDirPath = cacheDirPath;
    }

    public static FileUtils getInstance() {
        if (instance == null) {
            instance = new FileUtils();
        }
        return instance;
    }

    private static File sFile;

    public static boolean isUploadFileExist() {
        return getUploadFile().exists();
    }

    public static boolean isUploadFileHasValidSize() {
        return getUploadFile().length() == Constants.UPLOAD_FILE_SIZE;
    }

    public static File getUploadFile() {
        return new File(DIRECTORY_PATH + "/" + Constants.UPLOAD_FILE_NAME);
    }

}
