package ua.alu.npj6.parser.visitors;

import java.util.ArrayList;

import ua.alu.npj6.parser.model.ParserContext;
import ua.alu.npj6.parser.model.HandFile;
import ua.alu.npj6.parser.model.Line;
import ua.alu.npj6.parser.model.Expression;

import ua.alu.npj6.parser.PredicateBaseVisitor;
import ua.alu.npj6.parser.PredicateParser.HandFileContext;

public class HandFileVisitor extends PredicateBaseVisitor<HandFile> {
    LineVisitor lineVisitor;
    ParserContext parserContext;

    public HandFileVisitor(ParserContext parserContext) {
        this.parserContext = parserContext;
        lineVisitor = new LineVisitor(parserContext);
    }

    @Override
    public HandFile visitHandFile(HandFileContext ctx) {
        ArrayList<String> names = new ArrayList<>();
        ArrayList<Expression> expressions = new ArrayList<>();

        for (int i=0; i<ctx.getChildCount()-1; i++) {
            Line l = lineVisitor.visit(ctx.getChild(i));
            if (l.expr != null) {
                names.add(l.name);
                expressions.add(l.expr);
            } else if (l.role != null) {
                parserContext.roleNames.add(l.name);
                parserContext.roles.add(l.role);
            }
            
        }

        return new HandFile(names, expressions);
    }
}