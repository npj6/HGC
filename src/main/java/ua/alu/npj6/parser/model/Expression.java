package ua.alu.npj6.parser.model;

import java.util.List;

public class Expression {
    Condition condition = null;
    int draws;

    List<Expression> expressions = null;
    String operation = null;

    public Expression (Condition condition, int draws) {
        this.condition = condition;
        this.draws = draws;
    }

    public Expression (List<Expression> expressions, String operation) {
        this.expressions = expressions;
        this.operation = operation;
    }

    public void add (Expression expression) {
        this.expressions.add(expression);
    }

    public String operation() {
        return this.operation;
    }

    @Override
    public String toString() {
        String out = "";
        if (condition != null) {
            out += "@" + Integer.toString(draws) + "\n\t" + condition.toString().replaceAll("\n", "\n\t");
        }
        if (expressions != null) {
            out += "Expresion "+operation;
            for (Expression exp : expressions) {
                out += "\n\t" + exp.toString().replaceAll("\n", "\n\t");
            }
        }

        return out;
    }
}