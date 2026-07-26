package ua.alu.npj6.shuffler;


import java.util.function.BiPredicate;
import java.util.concurrent.Callable;

import java.util.concurrent.ThreadLocalRandom;

import ua.alu.npj6.Decklist;
import ua.alu.npj6.utils.NextInt;

import java.util.Arrays;

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
    boolean shuffleDrawAndCheck(int [] hand, int order[]) {
        int n;
        order[hand.length] = -1;
        for(int i=0; i<draws; i++) {
            n = nextInt.get(deck.list.length-i);
            int idx = hand.length;
            int lastIdx = -1;
            do {
                if (order[idx] == -1) {
                    //last item of list
                    order[i] = -1;
                    order[idx] = i;
                    break;
                } else if (hand[order[idx]] <= n) {
                    //item leq
                    n++;
                } else {
                    //item grt
                    order[i] = order[idx];
                    order[idx] = i;
                    break;
                }
                lastIdx = idx;
                idx = order[idx];
            } while (true);
            hand[i] = n;
        }
        return check.test(deck, hand);
    }

    @Override
    public Long call() {
        long count = 0L;
        int[] hand = new int[draws];
        int[] order = new int[draws+1];
        for(long l=0; l<workload; l++) {
            if (shuffleDrawAndCheck(hand, order)) {
                count++;
            }
        }
        return count;
    }

}