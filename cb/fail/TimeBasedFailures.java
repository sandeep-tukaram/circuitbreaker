package cb.fail;

public class TimeBasedFailures implements Failure {


    private final long TIME_WINDOW;
    private final int THRESHOLD;
    private int count;
    private long window_start;

    public TimeBasedFailures(int threshold, long timeWindow) {
        this.THRESHOLD = threshold;
        this.TIME_WINDOW = timeWindow;
    }

    @Override
    public boolean hitThreshold() {
        if (System.currentTimeMillis() - window_start < TIME_WINDOW) {
            if (count > THRESHOLD) return true;
        } else {
            // new window
            reset();
        }
        return false;
    }

    @Override
    public int reset() {
        // start a new window;
        this.window_start = System.currentTimeMillis();
        this.count = 0;
        return this.count;
    }

    @Override
    public int increment(int val) {
        if (System.currentTimeMillis() - window_start < TIME_WINDOW) { 
            this.count += val;
        } else {
            reset();
        }
        this.count += val;
        return this.count;
    }
    
}
