package ua.alu.npj6.HGC.parser;

import org.antlr.v4.runtime.CharStreams;
import ua.alu.npj6.HGC.parser.PredicateLexer;
import org.antlr.v4.runtime.CommonTokenStream;
import ua.alu.npj6.HGC.parser.PredicateParser;

import java.io.IOException;

import ua.alu.npj6.HGC.parser.visitors.HandFileVisitor;
import ua.alu.npj6.HGC.parser.model.HandFile;
import ua.alu.npj6.HGC.parser.model.ParserContext;
import ua.alu.npj6.HGC.parser.model.Expression;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.RecognitionException;

import ua.alu.npj6.HGC.Decklist;

public class HGCParser {

    public boolean successful = true;

    PredicateParser parser = null;
    ParserContext parserContext = null;
    HandFile handFile = null;

    public HGCParser(String handfile, Decklist decklist, int hand) {
        try  {
            PredicateLexer lexer = new PredicateLexer(CharStreams.fromFileName(handfile));
            lexer.addErrorListener(ThrowingErrorListener.INSTANCE);
            parser = new PredicateParser(new CommonTokenStream(lexer));
            parser.addErrorListener(ThrowingErrorListener.INSTANCE);
        } catch (IOException e) {
            successful = false;
            e.printStackTrace();
        }

        parserContext = new ParserContext(decklist, hand);
        HandFileVisitor handFileVisitor = new HandFileVisitor(parserContext);

        try {
            handFile = handFileVisitor.visit(parser.handFile());
            //System.out.println(handFile);
            System.out.println("Canonical Form");
            for (Expression expr : handFile.expressions) {
                System.out.println(expr);
                System.out.println(expr.canonicalForm());
            }
        } catch (ParseCancellationException e) {
            successful = false;
            System.out.println("[ERROR] Parsing of hand file ended unexpectedly");
        }
    }

    public static class ThrowingErrorListener extends BaseErrorListener {

        public static final ThrowingErrorListener INSTANCE = new ThrowingErrorListener();

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
            int charPositionInLine, String msg, RecognitionException e) throws ParseCancellationException {
                throw new ParseCancellationException("line " + line + ":" + charPositionInLine + " " + msg);
        }
    }
}