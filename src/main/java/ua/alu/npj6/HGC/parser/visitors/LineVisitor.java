package ua.alu.npj6.HGC.parser.visitors;

import ua.alu.npj6.HGC.parser.model.ParserContext;
import ua.alu.npj6.HGC.parser.model.Line;

import ua.alu.npj6.HGC.parser.PredicateBaseVisitor;
import ua.alu.npj6.HGC.parser.PredicateParser.HandLineContext;
import ua.alu.npj6.HGC.parser.PredicateParser.RoleLineContext;
import ua.alu.npj6.HGC.parser.PredicateParser.ConditionLineContext;

public class LineVisitor extends PredicateBaseVisitor<Line> {
    RoleVisitor roleVisitor;
    ExpressionVisitor expressionVisitor;
    ConditionVisitor conditionVisitor;
    ParserContext parserContext;

    public LineVisitor(ParserContext parserContext) {
        this.parserContext = parserContext;
        roleVisitor = new RoleVisitor(parserContext);
        expressionVisitor = new ExpressionVisitor(parserContext);
        conditionVisitor = new ConditionVisitor(parserContext);
    }

    @Override
    public Line visitHandLine(HandLineContext ctx) {
        String name = ctx.getChild(0).getText();
        return new Line(name.substring(1, name.length() - 1), expressionVisitor.visit(ctx.getChild(2)), ctx.getChild(2).getText());
    }

    @Override
    public Line visitRoleLine(RoleLineContext ctx) {
        String name = ctx.getChild(0).getText();
        return new Line(name.substring(1, name.length() - 1), roleVisitor.visit(ctx.getChild(2)), ctx.getChild(2).getText());
    }

    @Override
    public Line visitConditionLine(ConditionLineContext ctx) {
        String name = ctx.getChild(1).getText();
        return new Line(name.substring(1, name.length() - 1), conditionVisitor.visit(ctx.getChild(3)), ctx.getChild(3).getText());
    }
}