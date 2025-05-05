package cb.state;
// Represents circuit state.

import java.util.Optional;
import java.util.concurrent.TimeoutException;

import cb.CircuitOpenException;
import retry.RetryThresholdException;

public interface CircuitState {

    public  <Q, S> Optional<S> handle(Q request) throws CircuitOpenException, InterruptedException, RetryThresholdException, TimeoutException;

}
