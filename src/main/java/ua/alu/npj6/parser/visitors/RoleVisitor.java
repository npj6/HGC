package ua.alu.npj6.parser.visitors;

import java.util.Arrays;
import java.util.ArrayList;

import ua.alu.npj6.parser.model.ParserContext;
import ua.alu.npj6.parser.model.Role;

import ua.alu.npj6.parser.PredicateBaseVisitor;
import ua.alu.npj6.parser.PredicateParser.SimpleRoleContext;
import ua.alu.npj6.parser.PredicateParser.MultiRoleContext;

public class RoleVisitor extends PredicateBaseVisitor<Role> {
    ParserContext parserContext;

    public RoleVisitor(ParserContext parserContext) {
        this.parserContext = parserContext;
    }

    private void add(ArrayList<String> names, String reference) {
        reference = reference.substring(1, reference.length() - 1);

        //add a new name in the deck, avoid duplicates
        if (Arrays.asList(parserContext.decklist.names).contains(reference)) {
            if (!names.contains(reference)) {
                names.add(reference);
            }
            return;
        }
        
        int idx = parserContext.roleNames.indexOf(reference);
        if (idx != -1) {
            //add all names for a named role
            for (String n : parserContext.roles.get(idx).names()) {
                //avoid duplicates, add new names
                if (!names.contains(n)) {
                    names.add(n);
                }
            }
        } else {
            //name not found
            if (!parserContext.missingNames.contains(reference)) {
                System.out.println("[WARNING] Name "+reference+" not previously defined");
                parserContext.missingNames.add(reference);
            }
        }
    }

    @Override
    public Role visitSimpleRole(SimpleRoleContext ctx) {
        ArrayList<String> names = new ArrayList<>();
        String name = ctx.getChild(0).getText();
        add(names, name);
        return new Role(names);
    }

    @Override
    public Role visitMultiRole(MultiRoleContext ctx) {
        ArrayList<String> names = new ArrayList<>();
        for (int i=1; i<ctx.getChildCount(); i+=2) {
            String name = ctx.getChild(i).getText();
            add(names, name);
        }
        return new Role(names);
    }
}