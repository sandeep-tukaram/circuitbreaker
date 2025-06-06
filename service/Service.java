package service;
// Represents a service. A calls service B. The circuitBreaker 
// Q -> reQuest object type
// S -> reSponse object type

import java.util.Optional;
import java.util.concurrent.TimeoutException;

public interface Service<Q, S> {
   Optional<S> run(Q request) throws ServiceException, TimeoutException;
}
