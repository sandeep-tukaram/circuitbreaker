package service;

public class ServiceException extends Exception {

    public ServiceException(String msg) {
        super(msg);
    }

    public ServiceException(String msg, Throwable throwable) {
        super(msg, throwable);
    }

}
