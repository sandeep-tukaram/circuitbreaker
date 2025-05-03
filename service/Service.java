package service;
// Represents a service. A calls service B. The circuitBreaker 
// Q -> reQuest object type
// S -> reSponse object type

public interface Service {
   <Q, S> S run(Q request) throws ServiceException;
}
