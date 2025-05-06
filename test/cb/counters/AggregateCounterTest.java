package test.cb.counters;

import cb.counters.AggregateCounter;

public class AggregateCounterTest {
    
    public static void main(String[] args) throws InterruptedException {
        AggregateCounter counter = new AggregateCounter();

        counter.increment(10);
        Thread.sleep(21_000);
        counter.increment(5);

        counter.getAggregator().forEach((key, value) -> {
            System.out.println(key + " : " + value);
        });
    }
}
