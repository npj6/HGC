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
        ArrayList<Card> deckList = readDeck(file);
        ArrayList<Card> hand = new Shuffler().shuffleAndDraw(deckList, 7);
        for (int i=0; i<hand.size(); i++) {
            System.out.println(hand.get(i));
        }
    }

    private static ArrayList<Card> readDeck(File file) {
        ArrayList<Card> deckList = new ArrayList<>();

        try (Scanner sc = new Scanner(file)) {
            Pattern card = Pattern.compile("^\\s*(\\d+)\\s*x\\s*(.*\\S)\\s*$");
            Matcher matcher;
            while (sc.hasNextLine()) {
                String data = sc.nextLine();
                matcher = card.matcher(data);
                if (matcher.find()) {
                    deckList.add(new Card(matcher.group(2), Integer.parseInt(matcher.group(1))));
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        return deckList;
    }
}


