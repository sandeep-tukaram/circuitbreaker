package cb.counters;

import java.util.HashMap;
import java.util.Map;

public class AggregateCounter {
    private Map<TimeWindow, Integer> aggregator = new HashMap<TimeWindow, Integer>();
    private TimeWindow currentTimeWindow;

    public AggregateCounter() {
        this.currentTimeWindow = new TimeWindow(System.currentTimeMillis());
        this.aggregator.put(currentTimeWindow, 0);

    }

    public Integer increment(int val) {
        if (System.currentTimeMillis() - this.currentTimeWindow.getStartTime_MS() < this.currentTimeWindow.getWindowSize_MS()) {
            this.aggregator.put(this.currentTimeWindow, 
                                        aggregator.getOrDefault(currentTimeWindow, 0) + val);
        } else {
            // new time window with default size 10 s.
            this.currentTimeWindow = new TimeWindow(System.currentTimeMillis());
            this.aggregator.put(this.currentTimeWindow, val);
        }

        return this.aggregator.get(this.currentTimeWindow);
    }

    public Map<TimeWindow, Integer> getAggregator() {
        return aggregator;
    }

}
