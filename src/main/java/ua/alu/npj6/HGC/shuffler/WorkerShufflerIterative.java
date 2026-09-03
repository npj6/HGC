package ua.alu.npj6.HGC.shuffler;


import java.util.function.BiPredicate;
import java.util.concurrent.Callable;

import java.util.concurrent.ThreadLocalRandom;

import ua.alu.npj6.HGC.Decklist;
import ua.alu.npj6.HGC.utils.NextInt;

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

    int orderInsertionAndDisplacement(int [] hand, int order[], int i, int n) {
        int idx = hand.length;
        int lastIdx = -1;
        while (order[idx] != -1 && hand[order[idx]] <= n) {
            //item leq: continue iterating
            n++;
            lastIdx = idx;
            idx = order[idx];
        }
        
        //last item of list || item grt: insert and end
        order[i] = order[idx];
        order[idx] = i;
        return n;
    }

    @Override
    public Long call() {
        long count = 0L;
        int[] hand = new int[draws];
        int[] order = new int[draws+1];
        for(long l=0L; l<workload; l++) {
            order[hand.length] = -1;
            for(int i=0; i<draws; i++) {
                hand[i] = orderInsertionAndDisplacement(hand, order, i, nextInt.get(deck.list.length-i));
            }
            if (check.test(deck, hand)) {count++;}
        }
        return count;
    }

}