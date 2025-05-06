package cb.counters;

public class CountFailure implements Counter {
    private final int THRESHOLD;
    private int count;

    public CountFailure(int threshold) {
        this.THRESHOLD = threshold;
    }

    @Override
    public boolean hitThreshold() {
        return count > THRESHOLD;
    }

    @Override
    public int reset() {
        count = 0;
        return count;
    }

    @Override
    public int increment(int val) {
        count += val;
        return count;
    }
    
}
