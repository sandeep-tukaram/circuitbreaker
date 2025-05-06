package cb.counters;

// Named inspired by saw function in mathematics.
// Monotically increases from base to threshold and resets.
public class SawCounter implements Counter {
    private final int THRESHOLD;
    private int count;

    public SawCounter(int threshold) {
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
