package ua.alu.npj6.shuffler;

import java.util.function.BiPredicate;
import java.util.concurrent.Callable;

import java.util.concurrent.FutureTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.ArrayList;

import ua.alu.npj6.Decklist;
import ua.alu.npj6.utils.NextInt;

public class ConcurrentShuffler {

    WorkerShufflerFactory factory = new WorkerShufflerFactory() {
        @Override
        public Callable<Long> get(Decklist deck, int draws, BiPredicate<Decklist, int[]> check, long workload) {
            return new WorkerShufflerIterative(deck, draws, check, workload);
        }

        @Override
        public Callable<Long> get(Decklist deck, int draws, BiPredicate<Decklist, int[]> check, long workload, NextInt nextInt) {
            return new WorkerShufflerIterative(deck, draws, check, workload, nextInt);
        }
    };

    public long shuffleDrawAndCheck(Decklist deck, int draws, BiPredicate<Decklist, int[]> check, long hands, Strategy strat) {
        int threads = strat.threads.get();
        int workerN = strat.workers.get();
        ArrayList<FutureTask<Long>> workers = new ArrayList<>();
        
        for (int i=0; i<workerN-1; i++) {
            workers.add(new FutureTask<>(factory.get(deck, draws, check, hands/workerN)));
        }
        workers.add(new FutureTask<>(factory.get(deck, draws, check, hands - (workerN-1)*(hands/workerN))));

        ExecutorService executor = Executors.newFixedThreadPool(threads, (Runnable r) -> {
            Thread t = new Thread(r);
            t.setPriority(Thread.MAX_PRIORITY);
            return t;
        });
        
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