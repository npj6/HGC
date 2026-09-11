package ua.alu.npj6.HGC.parser;

import org.antlr.v4.runtime.CharStreams;
import ua.alu.npj6.HGC.parser.PredicateLexer;
import org.antlr.v4.runtime.CommonTokenStream;
import ua.alu.npj6.HGC.parser.PredicateParser;

import java.io.IOException;
import java.util.List;

import ua.alu.npj6.HGC.parser.visitors.HandFileVisitor;
import ua.alu.npj6.HGC.parser.model.HandFile;
import ua.alu.npj6.HGC.parser.model.ParserContext;
import ua.alu.npj6.HGC.parser.model.Expression;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.RecognitionException;

import ua.alu.npj6.HGC.Decklist;

import ua.alu.npj6.HGC.utils.Timer;

public class HGCParser {

    public boolean successful = true;

    PredicateParser parser = null;
    ParserContext parserContext = null;
    HandFile handFile = null;
    HandFile optimizedHandFile = null;

    public Expression getExpression(String name) {
        return optimizedHandFile.getExpression(name);
    }

    public String getText(String name) {
        return optimizedHandFile.getText(name);
    }

    public List<String> getNames() {
        return optimizedHandFile.names;
    }

    public HGCParser(String handfile, Decklist decklist) {
        try  {
            PredicateLexer lexer = new PredicateLexer(CharStreams.fromFileName(handfile));
            lexer.addErrorListener(ThrowingErrorListener.INSTANCE);
            parser = new PredicateParser(new CommonTokenStream(lexer));
            parser.addErrorListener(ThrowingErrorListener.INSTANCE);
        } catch (IOException e) {
            successful = false;
            e.printStackTrace();
            return;
        }

        parserContext = new ParserContext(decklist);
        HandFileVisitor handFileVisitor = new HandFileVisitor(parserContext);

        try {
            handFile = handFileVisitor.visit(parser.handFile());
        } catch (ParseCancellationException e) {
            successful = false;
            System.out.println("[ERROR] Parsing of hand file ended unexpectedly");
            successful = false;
            return;
        }

        

        handFile = Timer.time(() -> handFile.canonicalForm(), "canonical form");
        

        optimizedHandFile = Timer.time(() ->handFile.optimize(parserContext), "optimize");
        
        
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