package ua.alu;

import java.util.Random;
import java.util.function.BiFunction;


//Check efficiency and concurrency
class Shuffler {

    final private Random random;


    public Shuffler() {
        random = new Random();
    }

    public Shuffler(long seed) {
        random = new Random(seed);
    }

    public int[] shuffleAndDraw(Decklist deck, int draws) {
        int[] hand = new int[draws];

        shuffleAndDraw(deck, hand);

        return hand;
    }

    public void shuffleAndDraw(Decklist deck, int[] hand) {
        for(int i=0; i<hand.length; i++) {
            int n = random.nextInt(deck.list.length-i);
            int n2 = n;
            for (int j=0; j<i; j++) {
                if (hand[j] <= n) {
                    n2++;
                }
            }
            hand[i] = n2;
        }
    }

    public boolean shuffleDrawAndCheck(Decklist deck, int draws, BiFunction<Decklist, int[], Boolean> check) {
        return check.apply(deck, shuffleAndDraw(deck, draws));
    }

    public boolean shuffleDrawAndCheck(Decklist deck, int[] hand, BiFunction<Decklist, int[], Boolean> check) {
        shuffleAndDraw(deck, hand);
        return check.apply(deck, hand);
    }
}