package cb;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import cb.counters.Counter;
import cb.state.CircuitClosed;
import cb.state.CircuitHalfOpen;
import cb.state.CircuitOpen;
import cb.state.CircuitState;
import cb.state.CircuitStateEnum;
import event.Event;
import event.EventBus;
import metrics.CBMetrics;
import retry.RetryConfig;
import retry.RetryThresholdException;
import service.Service;
import service.ServiceException;

public class CircuitBreaker {
    
    // ADR -> Dependency Inject configs and wrap service and fallback service.
    private final CircuitBreakerConfig configs;
    private final RetryConfig retryConfig;      // ADR -> CB encapsulates Retry.
    private final Service service;      // ADR -> coupled service.
    private final Counter failureStrategy; // SawCounter or TimeWindowCounter
    private final Service fallBack;

    // Metrics
    private final CBMetrics metrics;

    // ADR -> encapsulated circuit states directed graph
    private CircuitState circuitOpen, circuitHalfOpen, circuitClosed, currentState;


    // ADR -> event system for circuit state
    private EventBus<CircuitState> eventBus;


    private CircuitBreaker(CircuitBreakerConfig configs, RetryConfig retryConfig, Service service, Counter failureStrategy, Service fallBack) {
        this.configs = configs;
        this.service = service;
        this.retryConfig = retryConfig;
        this.failureStrategy = failureStrategy;
        this.fallBack = fallBack;
        this.metrics = new CBMetrics();
    }

    // Factory method - better than an init(), which a client may fail to invoke.
    public static CircuitBreaker getInstance(CircuitBreakerConfig configs, RetryConfig retryConfig, Service service,  Counter failureStrategy, Service fallBack) {
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

        // publish state change
        this.eventBus.publish(new Event<CircuitState>(this.currentState));
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

    public Counter getFailureStrategy() {
        return failureStrategy;
    }

    public Service getFallBack() {
        return fallBack;
    }

    public CBMetrics getMetrics() {
        return metrics;
    }
}
