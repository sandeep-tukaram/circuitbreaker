import java.util.Optional;

public class CircuitBreaker<Q> {
    
    private CircuitState circuitState = CircuitState.CLOSED;
    private CircuitBreakerConfig configs;
    private long openTime;

    public CircuitBreaker(CircuitBreakerConfig configs) {
        this.configs = configs;
    }

    public  <Q, S> Optional<S> execute(Service service, Q request) throws CircuitOpenException, RetryThresholdException {
        if (this.circuitState == CircuitState.OPEN) {
            if ((System.currentTimeMillis() - openTime) < this.configs.OPEN_WAIT_TIME_MS) {
                // Accept no more reqeusts until open wait period
                throw new CircuitOpenException("Retry after - " + 
                                (this.configs.OPEN_WAIT_TIME_MS - ((System.currentTimeMillis() - openTime))));
            } 
            this.circuitState = CircuitState.HALF_OPEN;
        }
        
        return retry(service, request);
    }


    // Retry mechanism.
    public <Q, S> Optional<S> retry(Service service, Q request) throws RetryThresholdException {
        Optional<S> response = Optional.empty();
        int retry = 0;

        while(true) {
            try {
                response =  service.run(request);
                return response;
            } catch (ServiceException s) {
                if (circuitState == CircuitState.CLOSED && retry > this.configs.RETRY_THRESHOLD) {
                    throw new RetryThresholdException("Retry threshold hit!");
                } {
                    retry++;
                }
            }
        }
    }
}
