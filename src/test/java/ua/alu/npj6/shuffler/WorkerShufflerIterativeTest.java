package ua.alu.npj6.shuffler;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.function.BiPredicate;
import java.util.stream.Stream;

import ua.alu.npj6.Decklist;
import ua.alu.npj6.utils.NextInt;

public class WorkerShufflerIterativeTest {

    class mockBiPredicate implements BiPredicate<Decklist, int[]> {
            long counter = 0;

            public boolean test(Decklist deck, int[] hand) {
                counter++;
                return true;
            }
    };

    @Test
    public void callTest() {
        long workload = 10;
        mockBiPredicate check = new mockBiPredicate();
        WorkerShufflerIterative SUT = new WorkerShufflerIterative(
            new Decklist(new ArrayList<>(), new ArrayList<>()),
            0,
            check,
            workload
        );
        assertEquals(workload, SUT.call());
        assertEquals(workload, check.counter);
    }
    
    @ParameterizedTest
    @MethodSource("arrayProvider")
    public void shuffleDrawAndCheckTest(List<Integer> source) {
        int hand[] = new int[7];
        Decklist deck = new Decklist(new ArrayList<>(), new ArrayList<>());
        WorkerShufflerIterative SUT = new WorkerShufflerIterative(
            deck,
            hand.length,
            (Decklist d, int[] h) -> { assertSame(deck, d); assertSame(hand, h); return true; },
            0,
            new NextInt() {
                int counter = 0;

                public int get(int i) {
                    return source.get(counter++);
                }
            }
        );
        assertEquals(true, SUT.shuffleDrawAndCheck(hand));
        for (int i=0; i<hand.length; i++) {
            int val = i; //i is not final, lambda needs final
            assertTrue(Arrays.stream(hand).anyMatch(x -> x == val));
        }
    }

    //Asumes the random next int will be chosen from 0 to number of unchosen cards
    static Stream<List<Integer>> arrayProvider() {
        return Stream.of(
            Arrays.asList(0, 0, 0, 0, 0, 0, 0),
            Arrays.asList(6, 5, 4, 3, 2, 1, 0),
            Arrays.asList(3, 2, 2, 1, 0, 0, 0)
        );
    }
}