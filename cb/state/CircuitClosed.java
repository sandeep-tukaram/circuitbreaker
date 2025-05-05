package cb.state;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import cb.CircuitBreaker;
import cb.CircuitBreakerConfig;
import cb.CircuitOpenException;
import retry.Retry;
import retry.RetryConfig;
import retry.RetryThresholdException;
import service.Service;
import service.ServiceException;

public class CircuitClosed implements CircuitState {

    private final CircuitBreaker circuitBreaker;
    private int failCounter;

    public CircuitClosed(CircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public <Q, S> Optional<S> handle(Q request) throws ServiceException, CircuitOpenException, RetryThresholdException, InterruptedException, TimeoutException {
        Service service = this.circuitBreaker.getService();
        CircuitBreakerConfig configs = this.circuitBreaker.getConfigs();
        RetryConfig retryConfig = this.circuitBreaker.getRetryConfig();

        Optional<S> response = Optional.empty();

        while (true) {
            try {
                //Static Coupling -> transition state to OPEN
                if (this.failCounter > configs.getSERVICE_FAILURE_THRESHOLD()) {
                    response = this.circuitBreaker.transition(CircuitStateEnum.OPEN).handle(request);
                }

                response = Retry.handle(service, request, retryConfig);
                this.failCounter = 0;   // reset failure counts upon successful service response.
                break;
            } catch (Exception e) {
                this.failCounter = failCounter +  retryConfig.getRETRY_THRESHOLD();
                throw e;
            }
        }

        return response;
    }

}
