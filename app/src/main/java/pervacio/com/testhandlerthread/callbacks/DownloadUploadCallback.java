package pervacio.com.testhandlerthread.callbacks;

import pervacio.com.testhandlerthread.utils.Constants;

public interface DownloadUploadCallback {

    void onActionStart(@Constants.MeasureTaskType int taskType);

    void onActionProgress(@Constants.MeasureTaskType int taskType, float progress);

    void onActionFinish(@Constants.MeasureTaskType int taskType);

    void onActionError(@Constants.MeasureTaskType int taskType, String message);
}
