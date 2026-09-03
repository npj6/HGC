package ua.alu.npj6.parser.visitors;

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

    private void add(ArrayList<Integer> indexes, String reference) {
        reference = reference.substring(1, reference.length() - 1);

        //add a new name in the deck, avoid duplicates
        int idx = parserContext.decklist.indexOf(reference);
        if (idx != -1) {
            if (!indexes.contains(idx)) {
                indexes.add(idx);
            }
            return;
        }
        
        int roleIdx = parserContext.roleNames.indexOf(reference);
        if (roleIdx != -1) {
            //add all names for a named role
            for (Integer i : parserContext.roles.get(roleIdx).indexes) {
                //avoid duplicates, add new names
                if (!indexes.contains(i)) {
                    indexes.add(i);
                }
            }
        } else {
            //name not found (stored as -(missingNameIdx+1))
            roleIdx = parserContext.missingNames.indexOf(reference); 
            if (roleIdx == -1) {
                System.out.println("[WARNING] Name "+reference+" not previously defined");
                parserContext.missingNames.add(reference);
                indexes.add(-parserContext.missingNames.size());
            } else {
                indexes.add(-(roleIdx+1));
            }
        }
    }

    @Override
    public Role visitSimpleRole(SimpleRoleContext ctx) {
        ArrayList<Integer> indexes = new ArrayList<>();
        String name = ctx.getChild(0).getText();
        add(indexes, name);
        return new Role(indexes);
    }

    @Override
    public Role visitMultiRole(MultiRoleContext ctx) {
        ArrayList<Integer> indexes = new ArrayList<>();
        for (int i=1; i<ctx.getChildCount(); i+=2) {
            String name = ctx.getChild(i).getText();
            add(indexes, name);
        }
        return new Role(indexes);
    }
}