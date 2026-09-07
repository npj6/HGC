package ua.alu.npj6.HGC.parser.visitors;

import java.util.ArrayList;

import ua.alu.npj6.HGC.parser.model.ParserContext;
import ua.alu.npj6.HGC.parser.model.HandFile;
import ua.alu.npj6.HGC.parser.model.Line;
import ua.alu.npj6.HGC.parser.model.Expression;

import ua.alu.npj6.HGC.parser.PredicateBaseVisitor;
import ua.alu.npj6.HGC.parser.PredicateParser.HandFileContext;

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
        ArrayList<String> texts = new ArrayList<>();

        for (int i=0; i<ctx.getChildCount()-1; i++) {
            Line l = lineVisitor.visit(ctx.getChild(i));
            if (l.expr != null) {
                int idx = parserContext.exprNames.indexOf(l.name);
                if (idx == -1) {
                    parserContext.exprNames.add(l.name);
                    parserContext.expressions.add(l.expr.deepCopy());

                    names.add(l.name);
                    expressions.add(l.expr);
                    texts.add(l.text);
                } else {
                    parserContext.expressions.set(idx, l.expr.deepCopy());

                    expressions.set(idx, l.expr);
                    texts.set(idx, l.text);
                }
            } else if (l.role != null) {
                int idx = parserContext.roleNames.indexOf(l.name);
                if (idx == -1) {
                    parserContext.roleNames.add(l.name);
                    parserContext.roles.add(l.role);
                } else {
                    parserContext.roles.set(idx, l.role);
                }
            } else if (l.cond != null) {
                int idx = parserContext.condNames.indexOf(l.name);
                if (idx == -1) {
                    parserContext.condNames.add(l.name);
                    parserContext.conditions.add(l.cond);
                } else {
                    parserContext.conditions.set(idx, l.cond);
                }
            }
            
        }

        return new HandFile(names, expressions, texts);
    }
}