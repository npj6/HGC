package ua.alu.npj6.parser.model;

public class Line {
    public String name;

    public Expression expr = null;
    public Role role = null;

    public Line(String name, Expression expr) {
        this.name = name;
        this.expr = expr;
    }

    public Line(String name, Role role) {
        this.name = name;
        this.role = role;
    }
}