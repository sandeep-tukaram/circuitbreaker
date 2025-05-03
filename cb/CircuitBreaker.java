package cb;
import java.util.Optional;

import cb.state.CircuitClosed;
import cb.state.CircuitHalfOpen;
import cb.state.CircuitOpen;
import cb.state.CircuitState;
import cb.state.CircuitStateEnum;
import retry.Retry;
import retry.RetryConfig;
import retry.RetryThresholdException;
import service.Service;

public class CircuitBreaker {
    
    private final CircuitBreakerConfig configs;
    private CircuitState circuitOpen, circuitHalfOpen, circuitClosed, currentState;

    // mapped/coupled service object.
    private final Service service;
    private final RetryConfig retryConfig;

    // private constructor. The state objects needs this object for instantiation.
    // Alternative is to provide an init() method, but a client may fail to invoke it.
    private CircuitBreaker(CircuitBreakerConfig configs, Service service, RetryConfig retryConfig) {
        this.configs = configs;
        this.service = service;
        this.retryConfig = retryConfig;
    }

    // Factory method
    public static CircuitBreaker getInstance(CircuitBreakerConfig configs, Service service, RetryConfig retryConfig) {
        CircuitBreaker cb_instance = new CircuitBreaker(configs, service, retryConfig);

        // Initialize and set all circuit state objects
        cb_instance.circuitOpen = new CircuitOpen(cb_instance);
        cb_instance.circuitHalfOpen = new CircuitHalfOpen(cb_instance);
        cb_instance.circuitClosed = new CircuitClosed(cb_instance);

        return cb_instance;
    }

    public  <Q, S> Optional<S> handle(Q request) throws CircuitOpenException, InterruptedException, RetryThresholdException {
        return this.getState().handle(request);
    }

    public void transition(CircuitStateEnum circuitStateEnum) {
        switch (circuitStateEnum) {
            case OPEN:
                this.currentState = this.circuitOpen;
                ((CircuitOpen)this.circuitOpen).setOpenedInstant();
                break;
            case HALF_OPEN:
                this.currentState = this.circuitHalfOpen;
                break;
            default:
                this.currentState = this.circuitClosed;
        }
    }

    public CircuitState getState() {
        return this.currentState;
    }

    public Service getService() {
        return this.service;
    }

    public RetryConfig getRetryConfig() {
        return retryConfig;
    }

    public CircuitBreakerConfig getConfigs() {
        return this.configs;
    }
}
