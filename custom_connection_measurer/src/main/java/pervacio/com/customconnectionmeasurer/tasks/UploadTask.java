package pervacio.com.customconnectionmeasurer.tasks;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import pervacio.com.customconnectionmeasurer.IConnectionTypeChecker;
import pervacio.com.customconnectionmeasurer.TaskException;
import pervacio.com.customconnectionmeasurer.callbacks.TaskCallbacks;
import pervacio.com.customconnectionmeasurer.callbacks.UploadCallback;
import pervacio.com.customconnectionmeasurer.utils.Constants;
import pervacio.com.customconnectionmeasurer.utils.FileUtils;
import pervacio.com.customconnectionmeasurer.utils.MeasuringUnits;
import pervacio.com.customconnectionmeasurer.utils.RandomGen;

public class UploadTask extends AbstractCancelableTask {

    public static final String TAG = UploadTask.class.getSimpleName();

    private final String boundary;
    private static final String LINE_FEED = "\r\n";
    private HttpURLConnection httpConn;
    private String requestURL;
    private String charset;
    private OutputStream outputStream;
    private PrintWriter writer;

    private UploadCallback mUploadTaskCallback;
    private float mTotal;

    /**
     * This constructor initializes a new HTTP POST request with content type
     * is set to multipart/form-data. Also set maximum request duration
     *
     * @param requestURL
     * @param charset
     * @param duration
     * @throws IOException
     */
    public UploadTask(String requestURL, String charset, long duration, MeasuringUnits measuringUnit,
                      IConnectionTypeChecker checker, TaskCallbacks taskCallback) {
        super(duration, measuringUnit, checker);
        this.requestURL = requestURL;
        this.charset = charset;
        mUploadTaskCallback = taskCallback;
        // creates a unique boundary based on time stamp
        boundary = "===" + System.currentTimeMillis() + "===";
    }

    @Override
    protected void initBeforeStart() {
        if (!FileUtils.isUploadFileExist() || !FileUtils.isUploadFileHasValidSize()) {
            try {
                RandomGen.generateRandomFile(Constants.UPLOAD_FILE_SIZE);
            } catch (IOException e) {
                //TODO delay with that case
                Log.w("mUploadTaskCallback", "initBeforeStart:  " + e.getMessage());
                if (mUploadTaskCallback != null) {
                    mUploadTaskCallback.onUploadError(e.getMessage());
                }
            }
        }
    }

    @Override
    protected void onStart() {
        if (mUploadTaskCallback != null) {
            mUploadTaskCallback.onUploadStart();
        }
    }

    @Override
    void onProgress(float progress) {
        if (mUploadTaskCallback != null) {
            mUploadTaskCallback.onUploadProgress(progress);
        }
    }

    @Override
    void onFinish(float result) {
        if (mUploadTaskCallback != null) {
            mUploadTaskCallback.onUploadFinish(result);
        }
    }

    @Override
    void onError(String message) {
        if (mUploadTaskCallback != null) {
            mUploadTaskCallback.onUploadError(message);
        }
    }

    @Override
    protected float performAction() throws TaskException {
        intConnection(requestURL, charset);
        addHeaderField("User-Agent", "CodeJava");
        addHeaderField("Test-Header", "Header-Value");
        List<String> response = new ArrayList<>(0);
        try {
            mTotal = addFilePart("fileUpload", FileUtils.getUploadFile());
            response = finish();
            Log.d("UploadTask", "response : " + response);
        } catch (IOException e) {

            onError(e.getMessage());
        }
//        return TextUtils.join("\n", response);
        return mTotal;
    }

    private void intConnection(String requestURL, String charset) {
        URL url;
        try {
            url = new URL(requestURL);

            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setUseCaches(false);
            httpConn.setDoOutput(true); // indicates POST method
            httpConn.setDoInput(true);
            httpConn.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=" + boundary);
            httpConn.setRequestProperty("User-Agent", "CodeJava Agent");
            httpConn.setRequestProperty("Test", "Bonjour");
            httpConn.setChunkedStreamingMode(CHUNK_SIZE);
            outputStream = httpConn.getOutputStream();
            writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
        } catch (IOException e) {
            onError(e.getMessage());
        }
    }

    /**
     * Adds a form field to the request
     *
     * @param name  field name
     * @param value field value
     */
    public void addFormField(String name, String value) {
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"" + name + "\"")
                .append(LINE_FEED);
        writer.append("Content-Type: text/plain; charset=" + charset).append(
                LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
        writer.flush();
    }

    /**
     * Adds a upload file section to the request
     *
     * @param fieldName  name attribute in <input type="file" name="..." />
     * @param uploadFile a File to be uploaded
     * @throws IOException
     */
    public Float addFilePart(String fieldName, File uploadFile) throws IOException, TaskException {
        String fileName = uploadFile.getName();
        writer.append("--")
                .append(boundary)
                .append(LINE_FEED)
                .append("Content-Disposition: form-data; name=\"")
                .append(fieldName).append("\"; filename=\"")
                .append(fileName)
                .append("\"")
                .append(LINE_FEED)
                .append("Content-Type: ")
                .append(URLConnection.guessContentTypeFromName(fileName))
                .append(LINE_FEED)
                .append("Content-Transfer-Encoding: binary").append(LINE_FEED)
                .append(LINE_FEED)
                .flush();

        final float readBytes = readBytes(new FileInputStream(uploadFile), outputStream);

        writer.append(LINE_FEED);
        writer.flush();

        return readBytes;
    }

    /**
     * Adds a header field to the request.
     *
     * @param name  - name of the header field
     * @param value - value of the header field
     */
    public void addHeaderField(String name, String value) {
        writer.append(name).append(": ").append(value).append(LINE_FEED);
        writer.flush();
    }

    /**
     * Completes the request and receives response from the server.
     *
     * @return a list of Strings as response in case the server returned
     * status OK, otherwise an exception is thrown.
     * @throws IOException
     */
    public List<String> finish() throws IOException {
        List<String> response = new ArrayList<>();

        writer.append(LINE_FEED).flush();
        writer.append("--").append(boundary).append("--").append(LINE_FEED);
        writer.close();

        // checks server's status code first
        int status = httpConn.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    httpConn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                response.add(line);
            }
            reader.close();
            httpConn.disconnect();
        } else {
            throw new IOException("Server returned non-OK status: " + status);
        }

        return response;
    }

}
