package ua.alu;

import java.util.ArrayList;

class Decklist {
    final public String names[];
    final public int list[];

    public Decklist(ArrayList<String> cards, ArrayList<Integer> list) {
        this.names = cards.stream().toArray(String[]::new);
        this.list =  list.stream().mapToInt(i -> i).toArray();
    }
}