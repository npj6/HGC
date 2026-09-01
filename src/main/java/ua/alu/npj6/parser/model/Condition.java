package ua.alu.npj6.parser.model;

import java.util.List;

public class Condition {
    Restriction restriction = null;

    List<Condition> conditions = null;
    String operation = null; 

    public Condition (Restriction restriction) {
        this.restriction = restriction;
    }

    public Condition (List<Condition> conditions, String operation) {
        this.conditions = conditions;
        this.operation = operation;
    }

    public void add(Condition condition) {
        this.conditions.add(condition);
    }

    public String operation() {
        return this.operation;
    }

    @Override
    public String toString() {
        String out = "";

        if (restriction != null) {
            out += restriction.toString().replaceAll("\n", "\n\t");
        }
        if (conditions != null) {
            out += "Condition "+operation;
            for (Condition cond : conditions) {
                out += "\n\t" + cond.toString().replaceAll("\n", "\n\t");
            }
        }


        return out;
    }
}