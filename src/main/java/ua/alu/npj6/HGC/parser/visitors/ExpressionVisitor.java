package ua.alu.npj6.HGC.parser.visitors;

import java.util.ArrayList;

import ua.alu.npj6.HGC.parser.model.ParserContext;
import ua.alu.npj6.HGC.parser.model.Expression;
import ua.alu.npj6.HGC.parser.model.Condition;

import ua.alu.npj6.HGC.parser.PredicateBaseVisitor;
import ua.alu.npj6.HGC.parser.PredicateParser.SimpleExprContext;
import ua.alu.npj6.HGC.parser.PredicateParser.AndExprContext;
import ua.alu.npj6.HGC.parser.PredicateParser.OrExprContext;
import ua.alu.npj6.HGC.parser.PredicateParser.PExprContext;

public class ExpressionVisitor extends PredicateBaseVisitor<Expression> {
    ConditionVisitor conditionVisitor;
    ParserContext parserContext;

    public ExpressionVisitor(ParserContext parserContext) {
        this.parserContext = parserContext;
        conditionVisitor = new ConditionVisitor(parserContext);
    }

    @Override
    public Expression visitSimpleExpr(SimpleExprContext ctx) {
        int number = -1;
        try {
            number = Integer.parseInt(ctx.getChild(2).getText());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return new Expression(conditionVisitor.visit(ctx.getChild(0)), number);
    }

    @Override
    public Expression visitAndExpr(AndExprContext ctx) {
        ArrayList<Expression> expressions = new ArrayList<>();
        expressions.add(visit(ctx.getChild(0)));
        expressions.add(visit(ctx.getChild(2)));
        return new Expression(expressions, ctx.getChild(1).getText());
    }

    @Override
    public Expression visitOrExpr(OrExprContext ctx) {
        ArrayList<Expression> expressions = new ArrayList<>();
        expressions.add(visit(ctx.getChild(0)));
        expressions.add(visit(ctx.getChild(2)));
        return new Expression(expressions, ctx.getChild(1).getText());
    }

    @Override
    public Expression visitPExpr(PExprContext ctx) {
        return visit(ctx.getChild(1));
    }
}