package retry;

public class RetryConfig {
    private final long RETRY_WAIT_MS;
    private final int RETRY_THRESHOLD;

    public RetryConfig(long wait_ms, int count) {
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
