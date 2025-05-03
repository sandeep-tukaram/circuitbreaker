package cb.state;
import java.util.Map;
import java.util.Optional;

import cb.CircuitBreaker;
import cb.CircuitBreakerConfig;
import cb.Service;
import exceptions.CircuitOpenException;
import exceptions.RetryThresholdException;
import retry.Retry;

public class CircuitClosed implements CircuitState {

    private final CircuitBreaker circuitBreaker;
    private Map<Service, Integer> serviceFailCounter;

    public CircuitClosed(CircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public <Q, S> Optional<S> handle(Q request) throws CircuitOpenException, InterruptedException {
        Service service = this.circuitBreaker.getService();
        CircuitBreakerConfig configs = this.circuitBreaker.getConfigs();

        if(this.serviceFailCounter.get(service) == null) {
            this.serviceFailCounter.put(service, 0);
        }

        Optional<S> response = Optional.empty();

        while (true) {
            // Transistion from Closed to Open
            if (this.serviceFailCounter.get(service) > configs.SERVICE_FAILURE_THRESHOLD) {
                this.circuitBreaker.transition(CircuitStateEnum.OPEN);
                throw new CircuitOpenException("Service failure threshold hit");
            }


            try {
                response = Retry.handle(service, request, configs.RETRY_THRESHOLD, configs.RETRY_WAIT_MS);
                this.serviceFailCounter.put(service,0);    // reset failure counts upon successful service response.
                break;
            } catch (RetryThresholdException rt) {
                this.serviceFailCounter.compute(service, (key, value) ->value + configs.RETRY_THRESHOLD);
            }            
        }

        return response;
    }

}
