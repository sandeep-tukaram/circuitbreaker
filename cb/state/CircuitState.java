package cb.state;
// Represents circuit state.

import java.util.Optional;
import java.util.concurrent.TimeoutException;

import cb.CircuitOpenException;
import retry.RetryThresholdException;
import service.ServiceException;

public interface CircuitState {

    public  <Q, S> Optional<S> handle(Q request) throws ServiceException, CircuitOpenException, InterruptedException, RetryThresholdException, TimeoutException;

}
