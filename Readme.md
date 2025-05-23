
### Design Idea of a Circuit Breaker

Without Circuit Breaker
1. A (application)              --->                B(service)
                                (calls)  

## Happy Flow
All good, B (service) is available - serves all the request. Failure are intermittent, meaning retry requests get honored.

## Exception Flow - problems
Service A calls B, B throws exception upon multiple retries.  Two major concerns arise, if A continues calling a failing 
1. Network congestion 
2. B request queue is overloaded with requests that have high probability to throw exception. 

## Circuti Breaker  - solution
1. It's a client side solution (library).
2. It's a middleware/proxy that manages the clien't request.
3. It maintains a retry threshold, above which the retries are blocked. 
4. The block is partially removed after a certain configured open_time.  At this moment, the circuit is half-openend. Few of the requests are allowed. If the request exception reoccurs, the circuit is opened again. Step 4 runs in a loop. 
5. If at the half-opened, requests are succesful then the circuit is closed to allow normalcy.

## Circuit Breaker Design - State Machine
Two aspects of the state machine
1. State    ->   OPEN, HALF-OPEN, CLOSED
2. Transitions 

                 (a)
                ---->      OPEN 
        CLOSED            (b) | (c)
                <----     Half OPEN
                 (d)     

(a) Closed to Open -> The failure counts reach a threshold. The state transitions to Open.
(b) Open to Half Open -> The open circuit timeouts. The state transitions to half open.
(c) Half Open to Open -> The halfopen request fails. The state transitions back to open.
(d) Half Open to Close -> The halfopen request succeeds. The state transitions to closed. 

3. Entry states
    When a request is submitted to the circuit breaker, the state of the machine can any of the OPEN, CLOSED or HALF OPEN. The state machine has to handle the request accordingly. 

                                OPEN  <- A()
        A() ->  CLOSED 
                                HALF OPEN  <- A()     


4. External Service Request states
    a. Only HalfOpen and Closed state can make external service requests.

                        OPEN
       B() <--> CLOSED
                        HALF OPEN <--> B()

5. Return states
        Responses should be reqtured from any of the states. Conforms to the request-response model (point 3)
                                OPEN  -> A()
        A() <-  CLOSED 
                                HALF OPEN  -> A()     

                        
6. Conslidated Model 

                                 (2a)                              
                                ----->              OPEN  <---> A()   
        A()   <--->                                   |
                CLOSED                           (2b) | (2c)
        B()   <--->                                   |
                                <-----          Half OPEN <---> A()
                                  (2d)                    <---> B()

