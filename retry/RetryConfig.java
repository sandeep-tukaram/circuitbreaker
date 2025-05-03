package retry;

public class RetryConfig {
    public final long RETRY_WAIT_MS;
    public final int RETRY_THRESHOLD;

    RetryConfig(long wait_ms, int count) {
        this.RETRY_THRESHOLD = count;
        this.RETRY_WAIT_MS = wait_ms;
    }

    public int getRETRY_THRESHOLD() {
        return RETRY_THRESHOLD;
    }

    public long getRETRY_WAIT_MS() {
        return RETRY_WAIT_MS;
    }

}
