package state;
// Represents circuit state.

import java.util.Optional;

import exceptions.CircuitOpenException;
import exceptions.RetryThresholdException;

public interface CircuitState {


    public  <Q, S> Optional<S> handle(Q request) throws CircuitOpenException, InterruptedException, RetryThresholdException;

}
