package test.retry;


import java.util.Optional;

import retry.Retry;
import retry.RetryConfig;
import retry.RetryThresholdException;
import service.Service;
import service.ServiceException;

public class RetryTest {
    
    public static void main(String[] args) {
        success();
        failThreshold();
    }

    private static void success() {
        System.out.println("Test start -> success()");

        Service<String, Integer> service = request -> Optional.of(Integer.parseInt(request));
        RetryConfig config = new RetryConfig(10, 4);

        try{
            // 200 OK
            Optional<Integer> result = Retry.handle(service, "200", config);
            if (!result.isPresent() || result.get() != 200) {
                throw new AssertionError("Wrong result");
            }
        } catch (Exception e) {
            throw new AssertionError("Expected success");
        }

        System.out.println("Test finish -> success()");
    }


    private static void failThreshold() {
        System.out.println("Test start -> failThreshold()");

        // Passes the test
        Service<String, Integer> service = request -> {
            throw new ServiceException("500 Not available");
        };

        // Fails the test
        // Service<String, Integer> service = request -> {
        //     throw new RuntimeException("500 Not available");
        // };

        RetryConfig config = new RetryConfig(1000, 4);

        try{
            // 200 OK
            Optional<Integer> result = Retry.handle(service, "200", config);
            throw new AssertionError("Expected -> RetryThresholdException thrown");
        } catch (Exception e) {
            if (!(e instanceof RetryThresholdException)) 
                throw new AssertionError("Expected -> RetryThresholdException thrown");
        }

        System.out.println("Test finish -> failThreshold()");
    }


}
