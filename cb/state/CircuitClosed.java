package cb.state;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import cb.CircuitBreaker;
import cb.CircuitOpenException;
import cb.counters.Counter;
import retry.Retry;
import retry.RetryConfig;
import retry.RetryThresholdException;
import service.Service;
import service.ServiceException;

public class CircuitClosed implements CircuitState {

    private final CircuitBreaker circuitBreaker;

    public CircuitClosed(CircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public <Q, S> Optional<S> handle(Q request) throws ServiceException, CircuitOpenException, RetryThresholdException, InterruptedException, TimeoutException {
        Service service = this.circuitBreaker.getService();
        RetryConfig retryConfig = this.circuitBreaker.getRetryConfig();
        Counter failCounter = this.circuitBreaker.getFailureStrategy();

        Optional<S> response = Optional.empty();

        while (true) {
            try {
                //Static Coupling -> transition state to OPEN
                if (failCounter.hitThreshold()) {
                    response = this.circuitBreaker.transition(CircuitStateEnum.OPEN).handle(request);
                }

                response = Retry.handle(service, request, retryConfig);
                failCounter.reset();   // reset failure counts upon successful service response.
                break;
            } catch (Exception e) {
                failCounter.increment(retryConfig.getRETRY_THRESHOLD());
                throw e;
            }
        }

        return response;
    }

}
