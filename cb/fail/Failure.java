package cb.fail;

// Strategy pattern
public interface Failure {
    boolean hitThreshold();
    int reset();
    int increment(int val);
}