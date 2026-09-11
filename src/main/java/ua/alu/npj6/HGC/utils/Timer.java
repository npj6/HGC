package ua.alu.npj6.HGC.utils;

import java.util.function.Supplier;

public class Timer {

    static final double MEASURE = 1000000.0; //ms

    static public <T> T time(Supplier<T> func, String name) {
        long startTime, endTime, duration;
        startTime = System.nanoTime();
            T out = func.get();
        endTime = System.nanoTime();
        
        duration = (endTime - startTime);
        System.out.println("Duration "+name+": "+duration/MEASURE+" ms");
        System.out.println();
        return out;
    }

    long startTime, endTime, duration = 0;
    String name;
    long count = 0;

    public Timer(String name) {
        this.name = name;
    }

    //incurs in high overhead (147 ns per call estimated)
    public <T> T timeAcc(Supplier<T> func) {
        startTime = System.nanoTime();
            T out = func.get();
        endTime = System.nanoTime();
        count++;
        
        duration += (endTime - startTime);
        return out;
    }

    public void show() {
        System.out.println("Duration "+name+" : "+duration/MEASURE+" ms (count "+count+")");
        System.out.println();
    }
} 