package ua.alu.npj6.HGC.parser.model;

import java.util.List;
import java.util.ArrayList;

public class HandFile {
    List<String> names;
    List<Expression> expressions;

    public HandFile(List<String> names, List<Expression> expressions) {
        this.names = names;
        this.expressions = expressions;
    }

    public HandFile canonicalForm() {
        ArrayList<String> names = new ArrayList<>();
        ArrayList<Expression> expressions = new ArrayList<>();

        for (int i=0; i<this.names.size(); i++) {
            String name = this.names.get(i);
            Expression expr = this.expressions.get(i);
            
            int idx = names.indexOf(name);
            if (idx == -1) {
                names.add(name);
                expressions.add(expr.canonicalForm());
            } else {
                expressions.set(idx, expr.canonicalForm());
            }
        }

        return new HandFile(names, expressions);
    }

    //apply only after canonicalForm
    public HandFile optimize(ParserContext parserContext) {
        ArrayList<String> names = new ArrayList<>();
        ArrayList<Expression> expressions = new ArrayList<>();

        for(int i=0; i<this.names.size(); i++) {
            names.add(this.names.get(i));
            expressions.add(this.expressions.get(i).optimize(parserContext).canonicalForm());
        }

        return new HandFile(names, expressions);
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