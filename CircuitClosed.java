import java.util.Map;
import java.util.Optional;

public class CircuitClosed implements CircuitState {

    private Retry retry;
    private CircuitBreakerConfig configs;
    private Map<Service, Integer> serviceRetryCounter;

    public CircuitClosed(CircuitBreakerConfig configs) {
        this.configs = configs;
        this.retry = new Retry(configs);
    }

    @Override
    public <Q, S> Optional<S> handle(Service service, Q request) throws CircuitOpenException {

        if(this.serviceRetryCounter.get(service) == null) {
            this.serviceRetryCounter.put(service, 0);
        }

        Optional<S> response = Optional.empty();

        while (true) {
            if (this.serviceRetryCounter.get(service) > this.configs.SERVICE_FAILURE_THRESHOLD) {
                throw new CircuitOpenException("Service failure threshold hit");
            }
            try {
                response = this.retry.handle(service, request);
                this.serviceRetryCounter.put(service,0);    // reset failure counts upon successful service response.
                break;
            } catch (RetryThresholdException rt) {
                this.serviceRetryCounter.compute(service, (key, value) ->value + this.configs.RETRY_THRESHOLD);
            }            
        }

        return response;
    }

}
