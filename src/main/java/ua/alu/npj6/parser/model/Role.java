package ua.alu.npj6.parser.model;

import java.util.List;

public class Role {
    List<String> names;

    public Role(List<String> names) {
        this.names = names;
    }

    public List<String> names() {
        return names;
    }

    @Override
    public String toString() {
        return names.toString();
    }
}