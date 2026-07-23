package ua.alu.npj6.shuffler;

import java.util.function.Supplier;

public class Strategy {
    final public Supplier<Integer> threads;
    final public Supplier<Integer> workers;

    public Strategy(Supplier<Integer> threads, Supplier<Integer> workers) {
        this.threads = threads;
        this.workers = workers;
    }
}