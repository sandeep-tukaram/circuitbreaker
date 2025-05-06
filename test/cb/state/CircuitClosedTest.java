package test.cb.state;

import java.util.Optional;

import cb.CircuitBreaker;
import cb.CircuitBreakerConfig;
import cb.CircuitOpenException;
import cb.counters.SawCounter;
import cb.state.CircuitClosed;
import cb.state.CircuitOpen;
import retry.RetryConfig;
import service.Service;

public class CircuitClosedTest {
    public static void main(String[] args) {
        testSuccess();
        testCloseThreshold();
    }

    private static void testSuccess() {
        System.out.println("Test start -> testOpenCircuit()");

        Service<String, Integer> service = request -> Optional.of(Integer.parseInt(request));
        Service<String, Integer> fallback = request -> Optional.of(Integer.valueOf(200));

        RetryConfig retryconfig = new RetryConfig(10, 4);

        CircuitBreakerConfig configs = new CircuitBreakerConfig(10000, 3, 3);
        SawCounter failStrategy = new SawCounter(3);
        CircuitBreaker<String, Integer> circuitBreaker = CircuitBreaker.getInstance(configs, retryconfig, service , failStrategy, fallback);
 
        CircuitClosed<String, Integer> cb_closed = new CircuitClosed<String, Integer>(circuitBreaker);

        try{
            cb_closed.handle("200");

            // Check transition
            if  (!(circuitBreaker.getState() instanceof CircuitClosed)) 
                throw new AssertionError("Wrong state transition");
            
        } catch (Exception e) {
            // Check success
            throw new AssertionError("Expected -> Successful closed circuit request", e);
        }

        System.out.println("Test finish -> testOpenCircuit()");
    }


    private static void testCloseThreshold() {
        System.out.println("Test start -> testCloseThreshold()");

        Service<String, Integer> service = request -> Optional.of(Integer.parseInt(request));
        // Service<String, Integer> fallback = request -> Optional.of(Integer.valueOf(200));   // no fallback

        RetryConfig retryconfig = new RetryConfig(10, 4);

        CircuitBreakerConfig configs = new CircuitBreakerConfig(10000, 3, 3);
        SawCounter failStrategy = new SawCounter(2);
        CircuitBreaker<String, Integer> circuitBreaker = CircuitBreaker.getInstance(configs, retryconfig, service , failStrategy, null);
 
        CircuitClosed<String, Integer> cb_closed = new CircuitClosed<String, Integer>(circuitBreaker);
        failStrategy.increment(4);
        try{
            cb_closed.handle("200");
        } catch (Exception e) {
            // Circuit opens and throws exception.
            if (!(e instanceof CircuitOpenException)) {
                throw new AssertionError("Expected -> CircuitOpenException", e);
            }
        }

        // Check transition state
        if  (!(circuitBreaker.getState() instanceof CircuitOpen)) 
            throw new AssertionError("Expected ->  Circuit OPEN");
    
        System.out.println("Test finish -> testCloseThreshold()");
    }
}
