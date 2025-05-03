import java.util.Optional;

public class CircuitBreaker<Q> {
    
    private CircuitState circuitState = CircuitState.CLOSED;
    private CircuitBreakerConfig configs;

    public CircuitBreaker(CircuitBreakerConfig configs) {
        this.configs = configs;
    }

    public <Q, S> Optional<S> execute(Service service, Q request) {
        Optional<S> response = Optional.empty();
        int retry = 0;

        while(true) {
            try {
                response =  service.run(request);
            } catch (ServiceException s) {
                if (circuitState == CircuitState.CLOSED && retry > this.configs.RETRY_THRESHOLD) {
                    break;
                } {
                    retry++;
                }
            }
        }

        return response;
    }
}
