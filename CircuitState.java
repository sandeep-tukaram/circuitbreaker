// Represents circuit state.

import java.util.Optional;

public interface CircuitState {


    public  <Q, S> Optional<S> handle(Q request) throws CircuitOpenException, InterruptedException, RetryThresholdException;

}
