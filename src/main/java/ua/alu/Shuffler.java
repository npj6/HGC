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

    public int[] shuffleAndDraw(Decklist deck, int draws) {
        int[] hand = new int[draws];

        for(int i=0; i<draws; i++) {
            int n = random.nextInt(deck.list.length-i);
            int n2 = n;
            for (int j=0; j<i; j++) {
                if (hand[j] <= n) {
                    n2++;
                }
            }
            hand[i] = n2;
        }

        return hand;
    }
}