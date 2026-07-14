package ua.alu;

import java.util.ArrayList;
import java.util.Random;
import java.util.TreeSet;
import java.util.Iterator;


//Check efficiency and concurrency
class Shuffler {

    private Random random;

    public Shuffler() {
        random = new Random();
    }

    public Shuffler(long seed) {
        random = new Random(seed);
    }

    public ArrayList<Card> shuffleAndDraw(ArrayList<Card> deck, int draws) {
        int total = 0;
        for (Card c : deck) {
            total += c.number;
        }

        TreeSet<Integer> cards = new TreeSet<>();
        for(int i=0; i<draws; i++) {
            int n = random.nextInt(total-i);
            for (Integer c : cards) {
                if (c <= n) {
                    n++;
                }
            }
            cards.add(n);
        }

        ArrayList<Card> hand = new ArrayList<>();

        Iterator<Integer> it = cards.iterator();
        Integer draw = it.next();
        int acc = 0;
        int found = 0;

        for (int i=0; i<deck.size(); i++) {
            //Iterate through and count each draw of this card
            while(draw != null && draw < acc + deck.get(i).number) {
                found++;
                draw = it.hasNext() ? it.next() : null;
            }

            //If any found, add the card to the hand
            if (found != 0) {
                hand.add(new Card(deck.get(i).name, found));
                found = 0;
            }

            //Keep track of how many cards we have checked
            acc += deck.get(i).number;
        }

        return hand;
    }
}