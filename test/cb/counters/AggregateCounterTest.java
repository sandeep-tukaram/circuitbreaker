package test.cb.counters;

import cb.counters.AggregateCounter;

public class AggregateCounterTest {
    
    public static void main(String[] args) throws InterruptedException {
        AggregateCounter counter = new AggregateCounter();

        // Time window1 -> count 13
        counter.increment(10);
        Thread.sleep(4_000);
        counter.increment(3);

        // Time window2 -> count 5
        Thread.sleep(10_000);
        counter.increment(5);

        counter.getAggregator().forEach((key, value) -> {
            System.out.println(key + " : " + value);
        });
    }
}
