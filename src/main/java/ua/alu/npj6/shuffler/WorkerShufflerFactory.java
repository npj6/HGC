package ua.alu.npj6.shuffler;


import java.util.function.BiPredicate;
import java.util.concurrent.Callable;

import ua.alu.npj6.Decklist;
import ua.alu.npj6.utils.NextInt;

interface WorkerShufflerFactory {

    public Callable<Long> get(Decklist deck, int draws, BiPredicate<Decklist, int[]> check, long workload);

    public Callable<Long> get(Decklist deck, int draws, BiPredicate<Decklist, int[]> check, long workload, NextInt nextInt);

}