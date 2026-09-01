package ua.alu.npj6.parser.model;

import java.util.List;

public class HandFile {
    List<String> names;
    List<Expression> expressions;

    public HandFile(List<String> names, List<Expression> expressions) {
        this.names = names;
        this.expressions = expressions;
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