package ua.alu.npj6.shuffler;


import java.util.function.BiPredicate;
import java.util.concurrent.Callable;

import java.util.concurrent.ThreadLocalRandom;

import ua.alu.npj6.Decklist;
import ua.alu.npj6.utils.NextInt;

class WorkerShufflerIterative implements Callable<Long> {
    final private Decklist deck;
    final private int draws;
    final private BiPredicate<Decklist, int[]> check;
    final private long workload;
    final private NextInt nextInt;

    public WorkerShufflerIterative(Decklist deck, int draws, BiPredicate<Decklist, int[]> check, long workload) {
        this.deck = deck;
        this.draws = draws;
        this.check = check;
        this.workload = workload;
        this.nextInt = (i) -> ThreadLocalRandom.current().nextInt(i);
    }

    public WorkerShufflerIterative(Decklist deck, int draws, BiPredicate<Decklist, int[]> check, long workload, NextInt nextInt) {
        this.deck = deck;
        this.draws = draws;
        this.check = check;
        this.workload = workload;
        this.nextInt = nextInt;
    }

    //works better if you split the call function in two
    private boolean shuffleDrawAndCheck(int [] hand) {
        int n, n2;
        for(int i=0; i<draws; i++) {
            n = nextInt.get(deck.list.length-i);
            n2 = n;
            for (int j=0; j<i; j++) {
                if (hand[j] <= n) {
                    n2++;
                }
            }
            hand[i] = n2;
        }
        return check.test(deck, hand);
    }

    @Override
    public Long call() {
        long count = 0L;
        int[] hand = new int[draws];
        for(long l=0; l<workload; l++) {
            if (shuffleDrawAndCheck(hand)) {
                count++;
            }
        }
        return count;
    }

}