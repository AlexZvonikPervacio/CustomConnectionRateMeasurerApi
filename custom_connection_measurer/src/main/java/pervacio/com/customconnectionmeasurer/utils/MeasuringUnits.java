package pervacio.com.customconnectionmeasurer.utils;

public enum MeasuringUnits {

    B_S(1L, "B/s"), KB_S(1000L, "KB/s"), MB_S(1000 * 1000L, "MB/s"), GB_S(1000 * 1000 * 1000L, "GB/s"),
    bps(8L, "bps"), kbps(8 * 1000L, "kbps"), Mbps(8 * 1000 * 1000L, "Mbps"), Gbps(8 * 1000 * 1000 * 1000L, "Gbps"),
    KiB_S(1024L, "KiB/s"), MiB_S(1024 * 1024L, "MiB/s"), GiB_S(1024 * 1024 * 1024L, "GiB/s");

    private long mRate;
    private String mLabel;

    MeasuringUnits(long rate, String label) {
        mRate = rate;
        mLabel = label;
    }

    public long getRate() {
        return mRate;
    }

    public String getLabel() {
        return mLabel;
    }

    public float convertBytes(long totalLoaded, long timeSpent){
        return totalLoaded / mRate / (timeSpent / 1000f);
    }

}