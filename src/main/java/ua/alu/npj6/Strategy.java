package ua.alu.npj6;

import java.util.function.Supplier;

class Strategy {
    Supplier<Integer> threads;
    Supplier<Integer> workers;

    public Strategy(Supplier<Integer> threads, Supplier<Integer> workers) {
        this.threads = threads;
        this.workers = workers;
    }
}