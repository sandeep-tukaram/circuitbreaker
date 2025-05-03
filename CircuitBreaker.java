public class CircuitBreaker<Q> {
    
    public <Q, S> S execute(Service service, Q request) throws ServiceException {
        return service.run(request);
    }
}
