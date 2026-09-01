package ua.alu.npj6.parser.visitors;

import ua.alu.npj6.parser.model.ParserContext;
import ua.alu.npj6.parser.model.Restriction;
import ua.alu.npj6.parser.model.Role;

import ua.alu.npj6.parser.PredicateBaseVisitor;
import ua.alu.npj6.parser.PredicateParser.SimpleRestrictContext;
import ua.alu.npj6.parser.PredicateParser.TimesRestrictContext;
import ua.alu.npj6.parser.PredicateParser.ExactRestrictContext;

public class RestrictionVisitor extends PredicateBaseVisitor<Restriction> {
    RoleVisitor roleVisitor;
    ParserContext parserContext;

    public RestrictionVisitor(ParserContext parserContext) {
        this.parserContext = parserContext;
        roleVisitor = new RoleVisitor(parserContext);
    }

    @Override
    public Restriction visitSimpleRestrict(SimpleRestrictContext ctx) {
        return new Restriction(roleVisitor.visit(ctx.getChild(0)), false, 1);
    }
    
    @Override
    public Restriction visitTimesRestrict(TimesRestrictContext ctx) {
        int number = -1;
        try {
            number = Integer.parseInt(ctx.getChild(2).getText());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return new Restriction(roleVisitor.visit(ctx.getChild(0)), false, number);
    }
    
    @Override
    public Restriction visitExactRestrict(ExactRestrictContext ctx) {
        int number = -1;
        try {
            number = Integer.parseInt(ctx.getChild(2).getText());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return new Restriction(roleVisitor.visit(ctx.getChild(0)), true, number);
    }
}