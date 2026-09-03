package ua.alu.npj6.HGC.parser.visitors;

import java.util.ArrayList;

import ua.alu.npj6.HGC.parser.model.ParserContext;
import ua.alu.npj6.HGC.parser.model.Condition;
import ua.alu.npj6.HGC.parser.model.Restriction;

import ua.alu.npj6.HGC.parser.PredicateBaseVisitor;
import ua.alu.npj6.HGC.parser.PredicateParser.SimpleCondContext;
import ua.alu.npj6.HGC.parser.PredicateParser.AndCondContext;
import ua.alu.npj6.HGC.parser.PredicateParser.XAndCondContext;
import ua.alu.npj6.HGC.parser.PredicateParser.OrCondContext;
import ua.alu.npj6.HGC.parser.PredicateParser.PCondContext;

public class ConditionVisitor extends PredicateBaseVisitor<Condition> {
    RestrictionVisitor restrictionVisitor;
    ParserContext parserContext;

    public ConditionVisitor(ParserContext parserContext) {
        this.parserContext = parserContext;
        restrictionVisitor = new RestrictionVisitor(parserContext);
    }
    
    @Override
    public Condition visitSimpleCond(SimpleCondContext ctx) {
        return new Condition(restrictionVisitor.visit(ctx.getChild(0)));
    }

    @Override
    public Condition visitAndCond(AndCondContext ctx) {
        ArrayList<Condition> conditions = new ArrayList<>();
        conditions.add(visit(ctx.getChild(0)));
        conditions.add(visit(ctx.getChild(2)));
        return new Condition(conditions, ctx.getChild(1).getText());
    }
    
    @Override
    public Condition visitXAndCond(XAndCondContext ctx) {
        ArrayList<Condition> conditions = new ArrayList<>();
        conditions.add(visit(ctx.getChild(0)));
        conditions.add(visit(ctx.getChild(2)));
        return new Condition(conditions, ctx.getChild(1).getText());
    }
    
    @Override
    public Condition visitOrCond(OrCondContext ctx) {
        ArrayList<Condition> conditions = new ArrayList<>();
        conditions.add(visit(ctx.getChild(0)));
        conditions.add(visit(ctx.getChild(2)));
        return new Condition(conditions, ctx.getChild(1).getText());
    }
    
    @Override
    public Condition visitPCond(PCondContext ctx) {
        return visit(ctx.getChild(1));
    }
}