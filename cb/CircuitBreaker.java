package cb;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import cb.fail.Failure;
import cb.state.CircuitClosed;
import cb.state.CircuitHalfOpen;
import cb.state.CircuitOpen;
import cb.state.CircuitState;
import cb.state.CircuitStateEnum;
import retry.RetryConfig;
import retry.RetryThresholdException;
import service.Service;
import service.ServiceException;

public class CircuitBreaker {
    
    // ADR -> Dependency Inject
    private final CircuitBreakerConfig configs;
    private final RetryConfig retryConfig;      // ADR -> CB encapsulates Retry.
    private final Service service;      // ADR -> coupled service.
    private final Failure failureStrategy;
    private final Service fallBack;


    // ADR -> encapsulated circuit states directed graph
    private CircuitState circuitOpen, circuitHalfOpen, circuitClosed, currentState;

    private CircuitBreaker(CircuitBreakerConfig configs, RetryConfig retryConfig, Service service, Failure failureStrategy, Service fallBack) {
        this.configs = configs;
        this.service = service;
        this.retryConfig = retryConfig;
        this.failureStrategy = failureStrategy;
        this.fallBack = fallBack;
    }

    // Factory method - better than an init(), which a client may fail to invoke.
    public static CircuitBreaker getInstance(CircuitBreakerConfig configs, RetryConfig retryConfig, Service service,  Failure failureStrategy, Service fallBack) {
        CircuitBreaker cb_instance = new CircuitBreaker(configs,retryConfig, service, failureStrategy, fallBack);

        //  Static Coupling -> set all circuit states
        cb_instance.circuitOpen = new CircuitOpen(cb_instance);
        cb_instance.circuitHalfOpen = new CircuitHalfOpen(cb_instance);
        cb_instance.circuitClosed = new CircuitClosed(cb_instance);

        // Initialize current state to CLOSED
        cb_instance.currentState = cb_instance.transition(CircuitStateEnum.CLOSED);

        return cb_instance;
    }

    public  <Q, S> Optional<S> handle(Q request) throws ServiceException, CircuitOpenException, InterruptedException, RetryThresholdException, TimeoutException {
        return this.getState().handle(request);
    }

    public CircuitState transition(CircuitStateEnum circuitStateEnum) {
        switch (circuitStateEnum) {
            case OPEN:
                this.currentState = this.circuitOpen;
                ((CircuitOpen)this.circuitOpen).init();
                break;
            case HALF_OPEN:
                this.currentState = this.circuitHalfOpen;
                break;
            default:
                this.currentState = this.circuitClosed;
        }

        

        return this.currentState;
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

    public Failure getFailureStrategy() {
        return failureStrategy;
    }

    public Service getFallBack() {
        return fallBack;
    }
}
