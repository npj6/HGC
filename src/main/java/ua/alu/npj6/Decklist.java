package ua.alu.npj6;

import java.util.ArrayList;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Decklist {
    final public String names[];
    final public int quantities[];
    final public int list[];

    public Decklist(ArrayList<String> cards, ArrayList<Integer> list) {
        this.names = cards.stream().toArray(String[]::new);
        this.list =  list.stream().mapToInt(i -> i).toArray();

        ArrayList<Integer> quantities = new ArrayList<>();
        for(int i=0; i<this.names.length; i++) {
            int q = 0;
            for (int j=0; j<this.list.length; j++) {
                if (this.list[j] == i) {
                    q++;
                }
            }
            quantities.add(q);
        }

        this.quantities = quantities.stream().mapToInt(i -> i).toArray();
    }

    public int indexOf(String name) {
        int out = -1;

        for(int i=0; i<names.length; i++) {
            if (name.equals(names[i])) {
                out = i;
            }
        }

        return out;
    }

    public Decklist(File file) {
        ArrayList<String> cards = new ArrayList<>();
        ArrayList<Integer> list = new ArrayList<>();
        ArrayList<Integer> quantities = new ArrayList<>();

        try (Scanner sc = new Scanner(file)) {
            Pattern card = Pattern.compile("^\\s*(\\d+)\\s*x\\s*(.*\\S)\\s*$");
            Matcher matcher;
            while (sc.hasNextLine()) {
                String data = sc.nextLine();
                matcher = card.matcher(data);
                if (matcher.find()) {
                    cards.add(matcher.group(2));
                    quantities.add(Integer.parseInt(matcher.group(1)));
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
        this.quantities = quantities.stream().mapToInt(i -> i).toArray();
    }
}