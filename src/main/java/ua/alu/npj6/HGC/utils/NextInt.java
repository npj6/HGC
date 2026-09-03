package ua.alu.npj6.HGC.utils;

//Functional interface, slightly more efficient than Function<Integer, Integer>, allows Unit Testing
public interface NextInt{
    int get(int i);
}