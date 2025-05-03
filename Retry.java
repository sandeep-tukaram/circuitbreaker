import java.util.Optional;

public class Retry {

    public static <Q, S> Optional<S> handle(Service service, Q request, int RETRY_THRESHOLD, long RETRY_WAIT) throws RetryThresholdException, InterruptedException {
        Optional<S> response = Optional.empty();
        int retry = 0;

        while(true) {
            try {
                response =  service.run(request);
                return response;
            } catch (ServiceException s) {
                if (retry > RETRY_THRESHOLD) {
                    throw new RetryThresholdException("Retry threshold hit!", s);
                } {
                    retry++;
                }
            }
            Thread.sleep(RETRY_WAIT);
        }
    }

}
