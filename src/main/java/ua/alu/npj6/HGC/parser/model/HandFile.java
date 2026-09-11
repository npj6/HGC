package ua.alu.npj6.HGC.parser.model;

import java.util.List;
import java.util.ArrayList;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

import ua.alu.npj6.HGC.Decklist;

public class HandFile {
    public List<String> names;
    public List<Expression> expressions;
    public ArrayList<ArrayList<Role>> roleGroups;
    public List<String> texts;

    public HandFile(List<String> names, List<Expression> expressions, List<String> texts, ArrayList<ArrayList<Role>> roleGroups) {
        this.names = names;
        this.expressions = expressions;
        this.texts = texts;
        this.roleGroups = roleGroups;
    }

    public HandFile canonicalForm() {
        ArrayList<String> names = new ArrayList<>();
        ArrayList<Expression> expressions = new ArrayList<>();
        ArrayList<ArrayList<Role>> roleGroups = new ArrayList<>();
        ArrayList<String> texts = new ArrayList<>();

        for (int i=0; i<this.names.size(); i++) {
            String name = this.names.get(i);
            Expression expr = this.expressions.get(i);
            String text = this.texts.get(i);
            
            int idx = names.indexOf(name);
            if (idx == -1) {
                names.add(name);
                //System.out.println(name);
                //System.out.println(expr);
                ArrayList<Role> groups = expr.getRoleGroups();
                //System.out.println(groups);
                expr = expr.groupRoles(groups);
                //System.out.println(expr);
                Expression cF = expr.canonicalForm();
                expressions.add(cF);
                roleGroups.add(groups);
                //System.out.println(cF);
                texts.add(text);
            } else {
                ArrayList<Role> groups = expr.getRoleGroups();
                expr = expr.groupRoles(groups);
                expressions.set(idx, expr.canonicalForm());
                roleGroups.set(idx, groups);
                texts.set(idx, text);
            }
        }

        return new HandFile(names, expressions, texts, roleGroups);
    }

    //apply only after canonicalForm
    public HandFile optimize(ParserContext parserContext) {
        ArrayList<String> names = new ArrayList<>();
        ArrayList<Expression> expressions = new ArrayList<>();
        ArrayList<String> texts = new ArrayList<>();

        for(int i=0; i<this.names.size(); i++) {
            names.add(this.names.get(i));
            parserContext.roleGroups = roleGroups.get(i);
            expressions.add(this.expressions.get(i).optimize(parserContext).canonicalForm());
            texts.add(this.texts.get(i));
        }

        return new HandFile(names, expressions, texts, roleGroups);
    }

    public Expression getExpression(String name) {
        int idx = names.indexOf(name);

        if (idx == -1) {
            return null;
        } else {
            return expressions.get(idx);
        }
    }

    public String getText(String name) {
        int idx = names.indexOf(name);

        if (idx == -1) {
            return null;
        } else {
            return texts.get(idx);
        }
    }

    public Supplier<BiPredicate<Decklist, int[]>> getPredicate(Decklist decklist, String name) {
        int idx = names.indexOf(name);

        if (idx == -1) {
            return null;
        } else {
            return expressions.get(idx).getPredicate(decklist, roleGroups.get(idx));
        }
    }

    @Override
    public String toString() {
        String out = "";
        for(int i=0; i<names.size(); i++) {
            if (i !=0 ) {
                out += "\n";
            }
            out += names.get(i)
                + "\n\t" + expressions.get(i).toString().replaceAll("\n","\n\t");
        }
        return out;
    }
}