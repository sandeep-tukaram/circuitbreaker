package retry;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import service.Service;
import service.ServiceException;

public class Retry {

    public static <Q, S> Optional<S> handle(Service<Q, S> service, Q request, RetryConfig retryConfig) throws RetryThresholdException, InterruptedException, TimeoutException {
        Optional<S> response = Optional.empty();
        int retry = 0;

        while(true) {
            try {
                response =  service.run(request);
                return response;
            } catch (ServiceException s) {
                if (retry > retryConfig.getRETRY_THRESHOLD()) {
                    throw new RetryThresholdException("Retry threshold hit!", s);
                } {
                    retry++;
                }
            }
            Thread.sleep(retryConfig.getRETRY_WAIT_MS());
        }
    }

}
