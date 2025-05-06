package metrics;

import java.util.HashMap;
import java.util.Map;

import cb.counters.AggregateCounter;
import cb.state.CircuitStateEnum;

public class CBMetrics {

    private Map<CircuitStateEnum, AggregateCounter> successCounters = new HashMap<CircuitStateEnum, AggregateCounter>();    // Shouldn't include OPEN state.
    private Map<CircuitStateEnum, AggregateCounter> failureCounters = new HashMap<CircuitStateEnum, AggregateCounter>();

    public void recordSuccess(CircuitStateEnum circuitState, int successCount) {
        AggregateCounter successAggregator = this.successCounters.getOrDefault(circuitState, new AggregateCounter());
        successAggregator.increment(successCount);
        this.successCounters.put(circuitState, successAggregator);
    }

    public void recordFailure(CircuitStateEnum circuitState, int failCount) {
        AggregateCounter failAggregator = this.failureCounters.getOrDefault(circuitState, new AggregateCounter());
        failAggregator.increment(failCount);
        this.failureCounters.put(circuitState, failAggregator);
    }

}
