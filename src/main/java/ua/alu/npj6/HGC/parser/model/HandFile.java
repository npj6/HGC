package ua.alu.npj6.HGC.parser.model;

import java.util.List;
import java.util.ArrayList;

public class HandFile {
    public List<String> names;
    public List<Expression> expressions;
    public List<String> texts;

    public HandFile(List<String> names, List<Expression> expressions, List<String> texts) {
        this.names = names;
        this.expressions = expressions;
        this.texts = texts;
    }

    public HandFile canonicalForm() {
        ArrayList<String> names = new ArrayList<>();
        ArrayList<Expression> expressions = new ArrayList<>();
        ArrayList<String> texts = new ArrayList<>();

        for (int i=0; i<this.names.size(); i++) {
            String name = this.names.get(i);
            Expression expr = this.expressions.get(i);
            String text = this.texts.get(i);
            
            int idx = names.indexOf(name);
            if (idx == -1) {
                names.add(name);
                expressions.add(expr.canonicalForm());
                texts.add(text);
            } else {
                expressions.set(idx, expr.canonicalForm());
                texts.set(idx, text);
            }
        }

        return new HandFile(names, expressions, texts);
    }

    //apply only after canonicalForm
    public HandFile optimize(ParserContext parserContext) {
        ArrayList<String> names = new ArrayList<>();
        ArrayList<Expression> expressions = new ArrayList<>();
        ArrayList<String> texts = new ArrayList<>();

        for(int i=0; i<this.names.size(); i++) {
            names.add(this.names.get(i));
            expressions.add(this.expressions.get(i).optimize(parserContext).canonicalForm());
            texts.add(this.texts.get(i));
        }

        return new HandFile(names, expressions, texts);
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