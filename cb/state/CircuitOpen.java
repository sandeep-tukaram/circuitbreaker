package cb.state;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import cb.CircuitBreaker;
import cb.CircuitBreakerConfig;
import cb.CircuitOpenException;
import retry.RetryThresholdException;

// Represents Circuit Open state. 
// Handle transition. Open -> HalfOpen.
public class CircuitOpen implements CircuitState {

        private long openedInstant = -1l;   // -1l implicitly indicates circuit is not currently open.
        private final CircuitBreaker circuitBreaker;  

        public CircuitOpen(CircuitBreaker circuitBreaker) {
            this.circuitBreaker = circuitBreaker;
        }

        public  <Q, S> Optional<S> handle(Q request) throws CircuitOpenException, InterruptedException, RetryThresholdException, TimeoutException {
            CircuitBreakerConfig configs = this.circuitBreaker.getConfigs();

            // check open state
            if (openedInstant != -1l) {
                if ((System.currentTimeMillis() - openedInstant) <= configs.OPEN_WAIT_TIME_MS) {
                    // circuit is still open
                    throw new CircuitOpenException("Retry after - " + 
                                    (configs.OPEN_WAIT_TIME_MS - ((System.currentTimeMillis() - openedInstant))));
                } else {
                    // open timed out
                    this.openedInstant = -1l;
                }
            }

            //Static Coupling -> transition state to HalfOpen
            return this.circuitBreaker.transition(CircuitStateEnum.HALF_OPEN).handle(request);
        }

        // Initiatize opened state.
        public void init() {
            this.openedInstant = System.currentTimeMillis();
        }
    
}
