package ua.alu.npj6.HGC.shuffler;


import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.concurrent.Callable;

import ua.alu.npj6.HGC.Decklist;
import ua.alu.npj6.HGC.utils.NextInt;

interface WorkerShufflerFactory {

    public Callable<Long> get(Decklist deck, int draws, Supplier<BiPredicate<Decklist, int[]>> checkSupplier, long workload);

    public Callable<Long> get(Decklist deck, int draws, Supplier<BiPredicate<Decklist, int[]>> checkSupplier, long workload, NextInt nextInt);

}