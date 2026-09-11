package ua.alu.npj6.HGC;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

import java.util.Locale;

import ua.alu.npj6.HGC.shuffler.ConcurrentShuffler;
import ua.alu.npj6.HGC.shuffler.Strategy;

import ua.alu.npj6.HGC.parser.HGCParser;
import ua.alu.npj6.HGC.parser.model.Expression;

import ua.alu.npj6.HGC.parser.model.Role;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import ua.alu.npj6.HGC.utils.Timer;

public class App {

    static final long HANDS_N = 10000000L; //diez millones

    public static void main(String[] args) {

        ArgumentParser argParser = ArgumentParsers.newFor("HGC").build()
                .description("Game agnostic hand probability calculator.");
        argParser.addArgument("-dF", "--deckFile")
                .help("file containing deck information")
                .required(true);
        argParser.addArgument("-hF", "--handFile")
                .help("file containing hand definitions")
                .required(true);
        argParser.addArgument("--hands")
                .help("names of hands to be computed")
                .nargs("+");

        Namespace ns = null;
        try {
            ns = argParser.parseArgs(args);
        } catch (ArgumentParserException e) {
            argParser.handleError(e);
            System.exit(1);
        }

        File file = new File(ns.getString("deckFile"));

        Decklist decklist = new Decklist(file);
        String handFileName = ns.getString("handFile");
        
        HGCParser parser = Timer.time(() -> new HGCParser(handFileName, decklist), "total parse");

        if (!parser.successful) {
            System.out.println("Parsing unsuccessful");
            System.exit(1);
        }

        Strategy strat = new Strategy(
            () -> Runtime.getRuntime().availableProcessors(),
            () -> Runtime.getRuntime().availableProcessors()
        );

        System.out.println();
        System.out.println("Testing "+HANDS_N+" hands. Error is smaller than ±"
            +String.format(Locale.ENGLISH, "%2.4f", estimateError(HANDS_N)*100)+"% with 99% confidence.");
        System.out.println("Strategy with "+strat.threads.get()+" threads and "+strat.workers.get()+" workers");
        System.out.println();

        List<String> names = ns.<String> getList("hands");
        if (names == null) {
            names = parser.getNames();
        }

        for (String name : names) {
            Expression expr = parser.getExpression(name);
            String text = parser.getText(name);
            
            if (expr == null || text == null) {
                System.out.println("Name "+name+" not found.");
            } else {
                System.out.println(name+": "+text);
                System.out.println("HAND SIZE: " + expr.getDraws());
                System.out.println();
                calculateHand(expr, decklist, strat);
            }
        }
    }

    private static void calculateHand(Expression expr, Decklist decklist, Strategy strat) {
        int HAND_SIZE = expr.getDraws();
        Supplier<BiPredicate<Decklist, int[]>> checkSupplier = expr.getPredicate(decklist);

        int hand[] = new int[HAND_SIZE];
        long total = 0;
        ConcurrentShuffler shuffler = new ConcurrentShuffler();

        total = Timer.time(() -> shuffler.shuffleDrawAndCheck(decklist, HAND_SIZE, checkSupplier, HANDS_N, strat), "hand");
    }

    private static double estimateError(long hands) {
        return Math.sqrt(2 * Math.log(2/0.01)/(Math.log(2)*hands))/2;
    }
}


