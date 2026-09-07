package ua.alu.npj6.HGC.shuffler;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.concurrent.Callable;

import ua.alu.npj6.HGC.Decklist;
import ua.alu.npj6.HGC.utils.NextInt;

public class ConcurrentShufflerTest {
    @ParameterizedTest
    @ValueSource(ints = {1, 4, 6})
    public void workerNumberTest(int threads) {
        ConcurrentShuffler SUT = new ConcurrentShuffler();
        AtomicInteger counter = new AtomicInteger(); 
        WorkerShufflerFactory factory = new WorkerShufflerFactory() {

            @Override
            public Callable<Long> get(Decklist deck, int draws, Supplier<BiPredicate<Decklist, int[]>> checkSupplier, long workload) {
                return () -> (long) counter.getAndIncrement();
            }

            @Override
            public Callable<Long> get(Decklist deck, int draws, Supplier<BiPredicate<Decklist, int[]>> checkSupplier, long workload, NextInt nextInt) {
                return () -> (long) counter.getAndIncrement();
            }
        };
        SUT.factory = factory;

        assertEquals(0+1+2+3, SUT.shuffleDrawAndCheck(null, 0, null, 0, new Strategy(() -> threads, () -> 4)));
        assertEquals(4, counter.get());
    }
}