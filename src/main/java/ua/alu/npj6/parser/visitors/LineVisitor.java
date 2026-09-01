package ua.alu.npj6.parser.visitors;

import ua.alu.npj6.parser.model.ParserContext;
import ua.alu.npj6.parser.model.Line;

import ua.alu.npj6.parser.PredicateBaseVisitor;
import ua.alu.npj6.parser.PredicateParser.HandLineContext;
import ua.alu.npj6.parser.PredicateParser.RoleLineContext;

public class LineVisitor extends PredicateBaseVisitor<Line> {
    RoleVisitor roleVisitor;
    ExpressionVisitor expressionVisitor;
    ParserContext parserContext;

    public LineVisitor(ParserContext parserContext) {
        this.parserContext = parserContext;
        roleVisitor = new RoleVisitor(parserContext);
        expressionVisitor = new ExpressionVisitor(parserContext);
    }

    @Override
    public Line visitHandLine(HandLineContext ctx) {
        String name = ctx.getChild(0).getText();
        return new Line(name.substring(1, name.length() - 1), expressionVisitor.visit(ctx.getChild(2)));
    }

    @Override
    public Line visitRoleLine(RoleLineContext ctx) {
        String name = ctx.getChild(0).getText();
        return new Line(name.substring(1, name.length() - 1), roleVisitor.visit(ctx.getChild(2)));
    }
}