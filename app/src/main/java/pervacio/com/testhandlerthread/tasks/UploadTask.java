package pervacio.com.testhandlerthread.tasks;

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

import pervacio.com.testhandlerthread.callbacks.TaskCallbacks;
import pervacio.com.testhandlerthread.callbacks.UploadCallback;
import pervacio.com.testhandlerthread.utils.RandomGen;

import static pervacio.com.testhandlerthread.utils.RandomGen.FILE_SIZE;

public class UploadTask extends AbstractCancelableTask<Float> {

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
    public UploadTask(String requestURL, String charset, long duration, TaskCallbacks taskCallback) {
        super(duration, taskCallback);
        this.requestURL = requestURL;
        this.charset = charset;
        mUploadTaskCallback = taskCallback;
        if (!RandomGen.isFileExist()) {
            try {
                RandomGen.generateRandomFile(FILE_SIZE);
            } catch (IOException e) {
                if (mUploadTaskCallback != null) {
                    mUploadTaskCallback.onUploadError(e.getMessage());
                }
            }
        }
        // creates a unique boundary based on time stamp
        boundary = "===" + System.currentTimeMillis() + "===";
    }

    @Override
    protected void onStart() {
        if (mUploadTaskCallback != null){
            mUploadTaskCallback.onUploadStart();
        }
    }

    @Override
    protected Float performAction() {
        intConnection(requestURL, charset);
        addHeaderField("User-Agent", "CodeJava");
        addHeaderField("Test-Header", "Header-Value");
        List<String> response = new ArrayList<>(0);
        try {
            addFilePart("fileUpload", RandomGen.getFile());
            response = finish();
        } catch (IOException e) {
            if (mUploadTaskCallback != null) {
                mUploadTaskCallback.onUploadError(e.getMessage());
            }
        }
//        return TextUtils.join("", response);
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
            outputStream = httpConn.getOutputStream();
            writer = new PrintWriter(new OutputStreamWriter(outputStream, charset),
                    true);
        } catch (IOException e) {
            if (mUploadTaskCallback != null) {
                mUploadTaskCallback.onUploadError(e.getMessage());
            }
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
    public void addFilePart(String fieldName, File uploadFile)
            throws IOException {
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

        FileInputStream inputStream = new FileInputStream(uploadFile);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            if (!isCancelled()) {
                outputStream.write(buffer, 0, bytesRead);
            }
            mTotal += bytesRead;
            if (mUploadTaskCallback != null) {
                mUploadTaskCallback.onUploadProgress(mTotal);
            }
        }
        outputStream.flush();
        inputStream.close();

        if (mUploadTaskCallback != null) {
            mUploadTaskCallback.onUploadFinish(mTotal);
        }

        writer.append(LINE_FEED);
        writer.flush();
    }

    /**
     * Adds a header field to the request.
     *
     * @param name  - name of the header field
     * @param value - value of the header field
     */
    public void addHeaderField(String name, String value) {
        writer.append(name + ": " + value).append(LINE_FEED);
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
        writer.append("--" + boundary + "--").append(LINE_FEED);
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
