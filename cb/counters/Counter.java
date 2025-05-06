package cb.counters;

// Strategy pattern
public interface Counter {
    boolean hitThreshold();
    int reset();
    int increment(int val);
}