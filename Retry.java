import java.util.Optional;

// Retry mechanism
public class Retry {

    private CircuitBreakerConfig configs;

    public Retry(CircuitBreakerConfig configs) {
        this.configs = configs;
    }

    public <Q, S> Optional<S> handle(Service service, Q request) throws RetryThresholdException {
        Optional<S> response = Optional.empty();
        int retry = 0;

        while(true) {
            try {
                response =  service.run(request);
                return response;
            } catch (ServiceException s) {
                if (retry > this.configs.RETRY_THRESHOLD) {
                    throw new RetryThresholdException("Retry threshold hit!", s);
                } {
                    retry++;
                }
            }
        }
    }

}
