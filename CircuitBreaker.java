import java.util.Optional;

public class CircuitBreaker<Q> {
    
    private CircuitState circuitState = CircuitState.CLOSED;
    private CircuitBreakerConfig configs;
    private long openedInstant;  // instant when the circuit was first opened
    private int halfOpenRequestCount = 0; // halfOpen request counter
    private Retry retry;

    public CircuitBreaker(CircuitBreakerConfig configs) {
        this.configs = configs;
    }

    public  <Q, S> Optional<S> execute(Service service, Q request) throws CircuitOpenException, RetryThresholdException {
        // TODO -> Encapsulate state and transitions.
        // Transition -> Open to Half Open
        if (this.circuitState == CircuitState.OPEN) {
            if ((System.currentTimeMillis() - openedInstant) < this.configs.OPEN_WAIT_TIME_MS) {
                // Accept no more reqeusts until open wait period
                throw new CircuitOpenException("Retry after - " + 
                                (this.configs.OPEN_WAIT_TIME_MS - ((System.currentTimeMillis() - openedInstant))));
            } 
            this.circuitState = CircuitState.HALF_OPEN;
        }

        // TODD -> Looks buggy. Revisit the logic. 
        if (this.circuitState == CircuitState.HALF_OPEN && halfOpenRequestCount <= this.configs.HALF_OPEN_THRESHOLD) {
            try {
                return retry.handle(service, request);
            } finally {
                this.halfOpenRequestCount++;
            }
        }
        // Transition -> Half Open back to Open 
        else if (this.circuitState == CircuitState.HALF_OPEN && halfOpenRequestCount > this.configs.HALF_OPEN_THRESHOLD) {
            // maximum half open request threshold exhausted. Open the circuit
            this.circuitState = CircuitState.OPEN;
            this.halfOpenRequestCount = 0;
        } 
        // Transition -> Half Open to Closed
        else {
            this.circuitState = CircuitState.CLOSED;
            this.halfOpenRequestCount = 0;  // reset halfOpen request counter
        }

        // TODO handle transition from Closed to Open
        return retry.handle(service, request);
    }

}
