package cb;

public class CircuitBreakerConfig {

    // CB configs
    public final int HALF_OPEN_THRESHOLD;
    public final int SERVICE_FAILURE_THRESHOLD;
    public final long OPEN_WAIT_TIME_MS;


    public CircuitBreakerConfig (long open_wait_time_ms, int half_open_threshold, int service_failure_threshold) {
        this.HALF_OPEN_THRESHOLD = half_open_threshold;
        this.SERVICE_FAILURE_THRESHOLD = service_failure_threshold;
        this.OPEN_WAIT_TIME_MS = open_wait_time_ms;
    }

    public int getHALF_OPEN_THRESHOLD() {
        return HALF_OPEN_THRESHOLD;
    }

    public long getOPEN_WAIT_TIME_MS() {
        return OPEN_WAIT_TIME_MS;
    }

    public int getSERVICE_FAILURE_THRESHOLD() {
        return SERVICE_FAILURE_THRESHOLD;
    }

}
