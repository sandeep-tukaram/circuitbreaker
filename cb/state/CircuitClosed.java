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

public class CircuitClosed<Q,S>  implements CircuitState<Q,S>  {

    private final CircuitBreaker<Q,S>  circuitBreaker;

    public CircuitClosed(CircuitBreaker<Q,S>  circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public Optional<S> handle(Q request) throws ServiceException, CircuitOpenException, RetryThresholdException, InterruptedException, TimeoutException, IllegalAccessException {
        if(!(this.circuitBreaker.getState() instanceof CircuitClosed)) throw new IllegalAccessException("Circuit state is not Closed");

        
        Service<Q,S>  service = this.circuitBreaker.getService();
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
                this.circuitBreaker.getMetrics().recordSuccess(CircuitStateEnum.CLOSED, 1);
                
                break;
            } catch (Exception e) {
                failCounter.increment(retryConfig.getRETRY_THRESHOLD());
                this.circuitBreaker.getMetrics().recordFailure(CircuitStateEnum.CLOSED, retryConfig.getRETRY_THRESHOLD());
                throw e;
            }
        }

        return response;
    }

}
