package cb.counters;

public class TimeWindow {
    private final static int DEFAULT_WINDOW_SIZE_MS = 10000;    // 10s
    private final long startTime_MS;
    private final int windowSize_MS;

    public TimeWindow(long startTime) {
        this(startTime, TimeWindow.DEFAULT_WINDOW_SIZE_MS);
    }

    public TimeWindow(long startTime_MS, int windowSize_MS) {
        this.startTime_MS = startTime_MS;
        this.windowSize_MS = windowSize_MS;
    }

    public long getStartTime_MS() {
        return startTime_MS;
    }

    public int getWindowSize_MS() {
        return windowSize_MS;
    }

}
