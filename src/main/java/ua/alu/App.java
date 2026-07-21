package ua.alu;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import java.util.ArrayList;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.function.BiFunction;

//Try smaller workers with threadpool managing
public class App {
    public static void main(String[] args) {
        File file = new File(args[0]);
        Decklist deckList = readDecklist(file);

        final int HAND_SIZE = 7;
        final long HANDS_N = 100000000L; //cien millones
        final double MEASURE = 1000000.0; //ms
        BiFunction<Decklist, int[], Boolean> check = (Decklist d, int[] h) -> {
            boolean r = false;
            for(int i : h) {
                if (i == 0) {
                    r = true;
                }
            }
            return r;
        };

    
        Shuffler shuffler = new Shuffler();
        int hand[] = new int[HAND_SIZE];
        long total = 0;

        long startTime, endTime, duration;
    
        startTime = System.nanoTime();
            for (long i=0; i<HANDS_N; i++) {
                shuffler.shuffleAndDraw(deckList, hand);
                if (check.apply(deckList, hand)) {
                    total++;
                }
            }
        endTime = System.nanoTime();

        duration = (endTime - startTime);
        System.out.println("Duration: "+duration/MEASURE);
        System.out.println("Probability: "+total/(double) HANDS_N);
        System.out.println();


        ArrayList<Strategy> strats = new ArrayList<>();
        strats.add( new Strategy(() -> Runtime.getRuntime().availableProcessors(), () -> 4*Runtime.getRuntime().availableProcessors()));
        ConcurrentShuffler shuffler2 = new ConcurrentShuffler();
        for (Strategy strat : strats) {
            startTime = System.nanoTime();
                total = shuffler2.shuffleDrawAndCheck(deckList, HAND_SIZE, check, HANDS_N, strat);
            endTime = System.nanoTime();
                
            duration = (endTime - startTime);
            System.out.println("Strategy with "+strat.threads.get()+" threads and "+strat.workers.get()+" workers");
            System.out.println("Duration: "+duration/MEASURE);
            System.out.println("Probability: "+total/(double) HANDS_N);
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
}


