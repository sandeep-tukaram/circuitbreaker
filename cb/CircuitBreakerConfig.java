package cb;

public class CircuitBreakerConfig {

    public final long OPEN_WAIT_TIME_MS = 1000; // in milliseconds
    public final int RETRY_THRESHOLD = 5;
    public int HALF_OPEN_THRESHOLD = 10;
    public Integer SERVICE_FAILURE_THRESHOLD = 10;
    public long RETRY_WAIT_MS = 100;    // milliseconds
}
