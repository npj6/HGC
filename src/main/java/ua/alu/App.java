package ua.alu;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import java.util.ArrayList;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {
    public static void main(String[] args) {
        File file = new File(args[0]);
        Shuffler shuffler = new Shuffler();
        final int HAND_SIZE = 7;
        final int HANDS_N = 1000000000;

        int[] hand = null;
        Decklist deckList2 = readDecklist(file);
        long startTime = System.nanoTime();
        for (int i=0; i<HANDS_N; i++) {
            hand = shuffler.shuffleAndDraw(deckList2, HAND_SIZE);
        }
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println("Duration: "+duration/1000000.0);

        for (int i=0; i<hand.length; i++) {
            System.out.println(deckList2.names[deckList2.list[hand[i]]]);
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


