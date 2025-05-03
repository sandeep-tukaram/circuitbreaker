
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