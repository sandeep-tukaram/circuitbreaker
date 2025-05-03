package retry;

public class RetryThresholdException extends Exception {

    public RetryThresholdException(String msg) {
        super(msg);
    }

    public RetryThresholdException(String msg, Throwable embeddedException) {
        super(msg, embeddedException);
    }
    
}

