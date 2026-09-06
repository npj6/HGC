package ua.alu.npj6.HGC;

import java.io.File;

import java.util.ArrayList;

import java.util.function.BiPredicate;

import java.util.Locale;

import ua.alu.npj6.HGC.shuffler.ConcurrentShuffler;
import ua.alu.npj6.HGC.shuffler.Strategy;

import ua.alu.npj6.HGC.parser.HGCParser;

public class App {
    static void ANTLR4Test(Decklist decklist) {
        HGCParser parser = new HGCParser("src\\test\\resources\\test.hnd", decklist);

        if (parser.successful) {
            System.out.println("Parsing successful");
        } else {
            System.out.println("Parsing unsuccessful");
        }

        System.out.println(parser.getOptimizedHandFile());
    }

    public static void main(String[] args) {
        File file = new File(args[0]);
        Decklist decklist = new Decklist(file);

        final int HAND_SIZE = 7;

        ANTLR4Test(decklist);
        /*

        final long HANDS_N = 100000000L; //cien millones
        final double MEASURE = 1000000.0; //ms
        BiPredicate<Decklist, int[]> check = (Decklist deck, int[] hand) -> {
            boolean r = false;
            for(int i : hand) {
                if (i == 0) {
                    r = true;
                }
            }
            return r;
        };

        System.out.println("Testing "+HANDS_N+" hands. Error is smaller than ±"
            +String.format(Locale.ENGLISH, "%2.4f", estimateError(HANDS_N)*100)+"% with 99% confidence.");
       
        int hand[] = new int[HAND_SIZE];
        long total = 0;
        long startTime, endTime, duration;

        ArrayList<Strategy> strats = new ArrayList<>();
        strats.add( new Strategy(() -> Runtime.getRuntime().availableProcessors(), () -> Runtime.getRuntime().availableProcessors()));
        ConcurrentShuffler shuffler2 = new ConcurrentShuffler();
        for (Strategy strat : strats) {
            startTime = System.nanoTime();
                total = shuffler2.shuffleDrawAndCheck(decklist, HAND_SIZE, check, HANDS_N, strat);
            endTime = System.nanoTime();
                
            duration = (endTime - startTime);
            System.out.println("Strategy with "+strat.threads.get()+" threads and "+strat.workers.get()+" workers");
            System.out.println("Duration: "+duration/MEASURE+" ms");
            System.out.println("Probability: "+100*total/(double) HANDS_N+"%");
            System.out.println();
        }
        */
    }

    private static double estimateError(long hands) {
        return Math.sqrt(2 * Math.log(2/0.01)/(Math.log(2)*hands))/2;
    }
}


