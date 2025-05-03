import java.util.Optional;

public class CircuitBreaker<Q> {
    
    private CircuitState circuitState = CircuitState.CLOSED;
    private CircuitBreakerConfig configs;
    private long openedInstant;  // instant when the circuit was first opened
    private int halfOpenRequestCount = 0; // halfOpen request counter

    public CircuitBreaker(CircuitBreakerConfig configs) {
        this.configs = configs;
    }

    public  <Q, S> Optional<S> execute(Service service, Q request) throws CircuitOpenException, RetryThresholdException {
        // Transition -> Open to Half Open
        if (this.circuitState == CircuitState.OPEN) {
            if ((System.currentTimeMillis() - openedInstant) < this.configs.OPEN_WAIT_TIME_MS) {
                // Accept no more reqeusts until open wait period
                throw new CircuitOpenException("Retry after - " + 
                                (this.configs.OPEN_WAIT_TIME_MS - ((System.currentTimeMillis() - openedInstant))));
            } 
            this.circuitState = CircuitState.HALF_OPEN;
        }

        if (this.circuitState == CircuitState.HALF_OPEN && halfOpenRequestCount <= this.configs.HALF_OPEN_THRESHOLD) {
            try {
                return retry(service, request);
            } finally {
                this.halfOpenRequestCount++;
            }
        }
        // Transition -> Half Open back to Open 
        else if (this.circuitState == CircuitState.HALF_OPEN && halfOpenRequestCount > this.configs.HALF_OPEN_THRESHOLD) {
            // maximum half open request threshold exhausted. Open the circuit
            this.circuitState = CircuitState.OPEN;
            this.halfOpenRequestCount = 0;
        } else {
            this.circuitState = CircuitState.CLOSED;
            this.halfOpenRequestCount = 0;  // reset halfOpen request counter
        }

        // TODO - 
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
                    throw new RetryThresholdException("Retry threshold hit!", s);
                } {
                    retry++;
                }
            }
        }
    }
}
