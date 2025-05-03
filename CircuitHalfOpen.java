import java.util.Optional;

import exceptions.CircuitOpenException;
import exceptions.RetryThresholdException;

public class CircuitHalfOpen implements CircuitState {
        private final CircuitBreaker circuitBreaker;
        private int failCount;
        private int successCount;

        public CircuitHalfOpen(CircuitBreaker circuitBreaker) {
            this.circuitBreaker = circuitBreaker;
        }

        public  <Q, S> Optional<S> handle(Q request) throws CircuitOpenException, RetryThresholdException, InterruptedException {
            Service service = this.circuitBreaker.getService();
            CircuitBreakerConfig configs = this.circuitBreaker.getConfigs();

            // Open the circuit
            if (failCount >= configs.HALF_OPEN_THRESHOLD) {
                this.circuitBreaker.transition(CircuitStateEnum.OPEN);
                this.failCount = 0;
                throw new CircuitOpenException("Half open checks threashold hit");
            }

            

            Optional<S> response = Optional.empty();
            if (failCount < configs.HALF_OPEN_THRESHOLD) {
                try {
                    response =  Retry.handle(service, request, configs.RETRY_THRESHOLD, configs.RETRY_WAIT_MS);
                    this.successCount++;
                } finally {
                    this.failCount++;
                }
            }

            // Close the circuit
            if (this.successCount >= configs.HALF_OPEN_THRESHOLD) {
                this.successCount = 0;
                this.circuitBreaker.transition(CircuitStateEnum.CLOSED);
            }

            return response;
        }
}
