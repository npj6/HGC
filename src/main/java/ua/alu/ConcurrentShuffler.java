package ua.alu;

import java.util.function.BiPredicate;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.ArrayList;

import java.util.concurrent.ThreadLocalRandom;

class ConcurrentShuffler {

    class WorkerShuffler implements Callable<Long> {
        final private Decklist deck;
        final private int draws;
        final private BiPredicate<Decklist, int[]> check;
        final private long workload;

        public WorkerShuffler(Decklist deck, int draws, BiPredicate<Decklist, int[]> check, long workload) {
            this.deck = deck;
            this.draws = draws;
            this.check = check;
            this.workload = workload;
        }

        @Override
        public Long call() {
            long count = 0L;
            int[] hand = new int[draws];
            int n, n2;
            for(long l=0; l<workload; l++) {
                 for(int i=0; i<draws; i++) {
                    n = ThreadLocalRandom.current().nextInt(deck.list.length-i);
                    n2 = n;
                    for (int j=0; j<i; j++) {
                        if (hand[j] <= n) {
                            n2++;
                        }
                    }
                    hand[i] = n2;
                }
                
                if (check.test(deck, hand)) {
                    count++;
                }
            }
            return count;
        }

    }

    public Long shuffleDrawAndCheck(Decklist deck, int draws, BiPredicate<Decklist, int[]> check, long hands, Strategy strat) {
        int threads = strat.threads.get();
        int workerN = strat.workers.get();
        ArrayList<FutureTask<Long>> workers = new ArrayList<>();
        
        for (int i=0; i<workerN-1; i++) {
            workers.add(new FutureTask<>(new WorkerShuffler(deck, draws, check, hands/workerN)));
        }
        workers.add(new FutureTask<>(new WorkerShuffler(deck, draws, check, hands - (workerN-1)*(hands/workerN))));

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        for (FutureTask<Long> worker : workers) {
            executor.execute(worker);
        }

        long total = 0L;
        try {
            for (FutureTask<Long> worker : workers) {
                total += worker.get();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }

        return total;
    }

}