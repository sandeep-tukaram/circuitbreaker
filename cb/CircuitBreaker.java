package cb;
import java.util.Optional;

import exceptions.CircuitOpenException;
import exceptions.RetryThresholdException;
import state.CircuitClosed;
import state.CircuitHalfOpen;
import state.CircuitOpen;
import state.CircuitState;
import state.CircuitStateEnum;

public class CircuitBreaker {
    
    private final CircuitBreakerConfig configs;
    private CircuitState circuitOpen, circuitHalfOpen, circuitClosed, currentState;

    // mapped/coupled service object.
    private final Service service;

    // private constructor. The state objects needs this object for instantiation.
    // Alternative is to provide an init() method, but a client may fail to invoke it.
    private CircuitBreaker(CircuitBreakerConfig configs, Service service) {
        this.configs = configs;
        this.service = service;
    }

    // Factory method
    public static CircuitBreaker getInstance(CircuitBreakerConfig configs, Service service) {
        CircuitBreaker cb_instance = new CircuitBreaker(configs, service);

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

    public CircuitBreakerConfig getConfigs() {
        return this.configs;
    }

}
