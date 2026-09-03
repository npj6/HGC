package ua.alu.npj6.HGC.parser.model;

import java.util.List;
import java.util.ArrayList;

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

    public Expression canonicalForm() {
        if (condition != null ) {
            return new Expression(condition.canonicalForm(), draws);
        } else if (this.expressions != null) {
            ArrayList<Expression> expressions = new ArrayList<>();
            for (Expression expr : this.expressions) {
                expressions.add(expr.canonicalForm());
            }
            return new Expression(expressions, operation);
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        String out = "";
        if (condition != null) {
            out += "@" + Integer.toString(draws) + "\n\t" + condition.toString().replaceAll("\n", "\n\t");
        }
        if (expressions != null) {
            out += "Expression "+operation;
            for (Expression exp : expressions) {
                out += "\n\t" + exp.toString().replaceAll("\n", "\n\t");
            }
        }

        return out;
    }
}