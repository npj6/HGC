package ua.alu.npj6;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import java.util.ArrayList;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.function.BiPredicate;

import java.util.Locale;

public class App {
    public static void main(String[] args) {
        File file = new File(args[0]);
        Decklist deckList = readDecklist(file);

        final int HAND_SIZE = 7;
        final long HANDS_N = 100000000L; //cien millones
        final double MEASURE = 1000000.0; //ms
        BiPredicate<Decklist, int[]> check = (Decklist d, int[] h) -> {
            boolean r = false;
            for(int i : h) {
                if (i == 0) {
                    r = true;
                }
            }
            return r;
        };

        System.out.println("Testing "+HANDS_N+" hands. Error is smaller than ±"
            +String.format(Locale.ENGLISH, "%2.4f", estimateError(HANDS_N)*100)+"% with 99% confidence.");
       
    
        Shuffler shuffler = new Shuffler();
        int hand[] = new int[HAND_SIZE];
        long total = 0;

        long startTime, endTime, duration;
    
        startTime = System.nanoTime();
            for (long i=0; i<HANDS_N; i++) {
                shuffler.shuffleAndDraw(deckList, hand);
                if (check.test(deckList, hand)) {
                    total++;
                }
            }
        endTime = System.nanoTime();

        duration = (endTime - startTime);
        System.out.println("Duration: "+duration/MEASURE+" ms");
        System.out.println("Probability: "+100*total/(double) HANDS_N+"%");
        System.out.println();


        ArrayList<Strategy> strats = new ArrayList<>(); //10* might work better
        strats.add( new Strategy(() -> Runtime.getRuntime().availableProcessors(), () -> 4*Runtime.getRuntime().availableProcessors()));
        ConcurrentShuffler shuffler2 = new ConcurrentShuffler();
        for (Strategy strat : strats) {
            startTime = System.nanoTime();
                total = shuffler2.shuffleDrawAndCheck(deckList, HAND_SIZE, check, HANDS_N, strat);
            endTime = System.nanoTime();
                
            duration = (endTime - startTime);
            System.out.println("Strategy with "+strat.threads.get()+" threads and "+strat.workers.get()+" workers");
            System.out.println("Duration: "+duration/MEASURE+" ms");
            System.out.println("Probability: "+100*total/(double) HANDS_N+"%");
            System.out.println();
        }
    }

    private static Decklist readDecklist(File file) {
        ArrayList<String> cards = new ArrayList<>();
        ArrayList<Integer> list = new ArrayList<>();

        try (Scanner sc = new Scanner(file)) {
            Pattern card = Pattern.compile("^\\s*(\\d+)\\s*x\\s*(.*\\S)\\s*$");
            Matcher matcher;
            while (sc.hasNextLine()) {
                String data = sc.nextLine();
                matcher = card.matcher(data);
                if (matcher.find()) {
                    cards.add(matcher.group(2));
                    for (int i=0; i<Integer.parseInt(matcher.group(1)); i++) {
                        list.add(cards.size()-1);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        return new Decklist(cards, list);
    }

    private static double estimateError(long hands) {
        return Math.sqrt(2 * Math.log(2/0.01)/(Math.log(2)*hands))/2;
    }
}


