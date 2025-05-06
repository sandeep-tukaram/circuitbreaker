package cb.state;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import cb.CircuitBreaker;
import cb.CircuitBreakerConfig;
import cb.CircuitOpenException;
import cb.counters.Counter;
import retry.Retry;
import retry.RetryConfig;
import retry.RetryThresholdException;
import service.Service;
import service.ServiceException;

public class CircuitHalfOpen<Q,S> implements CircuitState<Q,S> {
        private final CircuitBreaker<Q,S> circuitBreaker;
        private int successCount;

        public CircuitHalfOpen(CircuitBreaker<Q,S> circuitBreaker) {
            this.circuitBreaker = circuitBreaker;
        }

        public Optional<S> handle(Q request) throws CircuitOpenException, RetryThresholdException, InterruptedException, TimeoutException, ServiceException {
            Service<Q,S>  service = this.circuitBreaker.getService();
            CircuitBreakerConfig configs = this.circuitBreaker.getConfigs();
            RetryConfig retryConfig = this.circuitBreaker.getRetryConfig();
            Counter failCounter = this.circuitBreaker.getFailureStrategy();

            Optional<S> response = Optional.empty();

            // Static coupling -> Transition to Open circuit when failures hit threshold
            if (failCounter.hitThreshold()) {
                failCounter.reset();
                response =  this.circuitBreaker.transition(CircuitStateEnum.OPEN).handle(request);
            }
            
            // Try request if half open failures is still less than the threshold.
            try {
                response =  Retry.handle(service, request, retryConfig);
                this.circuitBreaker.getMetrics().recordSuccess(CircuitStateEnum.HALF_OPEN, 1);
                this.successCount += retryConfig.getRETRY_THRESHOLD();
            } catch (Exception e) {
                this.circuitBreaker.getMetrics().recordFailure(CircuitStateEnum.HALF_OPEN, retryConfig.getRETRY_THRESHOLD());
                failCounter.increment(retryConfig.getRETRY_THRESHOLD());
                throw e;
            }

            // Static coupling -> Trasition to Closed circuit when success hit threshold
            if (this.successCount > configs.getHALF_OPEN_THRESHOLD()) {
                this.successCount = 0;
                response = this.circuitBreaker.transition(CircuitStateEnum.CLOSED).handle(request);
            }

            return response;
        }
}
