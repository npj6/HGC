package ua.alu.npj6.parser.model;

import java.util.List;
import java.util.ArrayList;

public class Role {
    List<String> names;

    public Role(List<String> names) {
        this.names = names;
    }

    public List<String> names() {
        return names;
    }

    public Role canonicalForm() {
        ArrayList<String> names = new ArrayList<>();

        for (String n : this.names) {
            names.add(n);
        }

        return new Role(names);
    }

    @Override
    public String toString() {
        return names.toString();
    }
}