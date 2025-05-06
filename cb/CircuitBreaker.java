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

public class CircuitBreaker<Q, S> {
    
    // ADR -> Dependency Inject configs and wrap service and fallback service.
    private final CircuitBreakerConfig configs;
    private final RetryConfig retryConfig;      // ADR -> CB encapsulates Retry.
    private final Service<Q, S> service;      // ADR -> coupled service.
    private final Counter failureStrategy; // SawCounter or TimeWindowCounter
    private final Service<Q, S> fallBack;

    // Metrics
    private final CBMetrics metrics;

    // ADR -> encapsulated circuit states directed graph
    private CircuitState<Q,S> circuitOpen, circuitHalfOpen, circuitClosed, currentState;


    // ADR -> event system for circuit state
    private EventBus<CircuitState<Q,S>> eventBus;


  

    private CircuitBreaker(CircuitBreakerConfig configs, RetryConfig retryConfig, Service<Q, S> service, Counter failureStrategy, Service<Q, S> fallBack) {
        this.configs = configs;
        this.retryConfig = retryConfig;
        this.failureStrategy = failureStrategy;

        this.service = service;
        this.fallBack = fallBack;

        this.eventBus = new EventBus<CircuitState<Q,S>>();
        this.metrics = new CBMetrics();
    }

    // Factory method - better than an init(), which a client may fail to invoke.
    public static <Q,S> CircuitBreaker<Q, S> getInstance(CircuitBreakerConfig configs, RetryConfig retryConfig, Service<Q,S> service,  Counter failureStrategy, Service<Q,S> fallBack) {
        CircuitBreaker<Q,S> cb_instance = new CircuitBreaker<Q,S>(configs,retryConfig, service, failureStrategy, fallBack);

        //  Static Coupling -> set all circuit states
        cb_instance.circuitOpen = new CircuitOpen<Q,S>(cb_instance);
        cb_instance.circuitHalfOpen = new CircuitHalfOpen<Q,S>(cb_instance);
        cb_instance.circuitClosed = new CircuitClosed<Q,S> (cb_instance);

        // Initialize current state to CLOSED
        cb_instance.currentState = cb_instance.transition(CircuitStateEnum.CLOSED);

        return cb_instance;
    }

    public Optional<S> handle(Q request) throws ServiceException, CircuitOpenException, InterruptedException, RetryThresholdException, TimeoutException, IllegalAccessException {
        return this.getState().handle(request);
    }

    public CircuitState<Q, S> transition(CircuitStateEnum circuitStateEnum) {
        switch (circuitStateEnum) {
            case OPEN:
                this.currentState = this.circuitOpen;
                ((CircuitOpen<Q,S> )this.circuitOpen).init();
                break;
            case HALF_OPEN:
                this.currentState = this.circuitHalfOpen;
                break;
            default:
                this.currentState = this.circuitClosed;
        }

        // publish state change
        this.eventBus.publish(new Event<CircuitState<Q,S> >(this.currentState));
        return this.currentState;
    }

    public CircuitState<Q, S> getState() {
        return this.currentState;
    }

    public Service<Q, S> getService() {
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

    public Service<Q,S> getFallBack() {
        return fallBack;
    }

    public CBMetrics getMetrics() {
        return metrics;
    }

    public EventBus<CircuitState<Q, S>> getEventBus() {
        return eventBus;
    }
}
