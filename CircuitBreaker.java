import java.util.Optional;

public class CircuitBreaker<Q> {
    
    private CircuitState circuitState = CircuitState.CLOSED;
    private CircuitBreakerConfig configs;

    public CircuitBreaker(CircuitBreakerConfig configs) {
        this.configs = configs;
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
