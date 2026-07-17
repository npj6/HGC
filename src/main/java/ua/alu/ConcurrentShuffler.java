package ua.alu;

import java.util.function.BiFunction;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.Random;
import java.util.ArrayList;

class ConcurrentShuffler {

    final private Random random;

    class WorkerShuffler implements Callable<Long> {
        final private Shuffler shuffler;
        final private Decklist deck;
        final private int draws;
        final private BiFunction<Decklist, int[], Boolean> check;
        final private long workload;

        public WorkerShuffler(long seed, Decklist deck, int draws, BiFunction<Decklist, int[], Boolean> check, long workload) {
            this.shuffler = new Shuffler(seed);
            this.deck = deck;
            this.draws = draws;
            this.check = check;
            this.workload = workload;
            System.out.println("Worker creado con workload "+workload);
        }

        @Override
        public Long call() {
            long count = 0L;
            int[] hand = new int[draws];
            for(long i=0; i<workload; i++) {
                if (shuffler.shuffleDrawAndCheck(deck, hand, check)) {
                    count++;
                }
            }
            return count;
        }

    }

    public ConcurrentShuffler() {
        this.random = new Random();
    }

    public ConcurrentShuffler(long seed) {
        this.random = new Random(seed);
    }

    public Long shuffleDrawAndCheck(Decklist deck, int draws, BiFunction<Decklist, int[], Boolean> check, long hands) {
        int threads = Runtime.getRuntime().availableProcessors() + 2;
        ArrayList<FutureTask<Long>> workers = new ArrayList<>();
        
        for (int i=0; i<threads-1; i++) {
            workers.add(new FutureTask<>(new WorkerShuffler(random.nextLong(), deck, draws, check, hands/threads)));
        }
        workers.add(new FutureTask<>(new WorkerShuffler(random.nextLong(), deck, draws, check, hands - (threads-1)*hands/threads)));

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