package ua.alu.npj6;

import java.util.ArrayList;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Decklist {
    final public String names[];
    final public int list[];

    public Decklist(ArrayList<String> cards, ArrayList<Integer> list) {
        this.names = cards.stream().toArray(String[]::new);
        this.list =  list.stream().mapToInt(i -> i).toArray();
    }

    public Decklist(File file) {
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

        this.names = cards.stream().toArray(String[]::new);
        this.list =  list.stream().mapToInt(i -> i).toArray();
    }
}