package ua.alu.npj6.parser.model;

import java.util.List;
import java.util.ArrayList;

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

    public Condition canonicalForm() {
        if (restriction != null) {
            return new Condition(restriction.canonicalForm());
        } else if (this.conditions != null) {
            ArrayList<Condition> conditions = new ArrayList<>();

            for (Condition cond : this.conditions) {
                conditions.add(cond.canonicalForm());
            }

            return new Condition(conditions, operation);
        }  else {
            return null;
        }
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