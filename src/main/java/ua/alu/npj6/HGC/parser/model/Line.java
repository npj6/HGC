package ua.alu.npj6.HGC.parser.model;

public class Line {
    public String name;
    public String text;

    public Expression expr = null;
    public Role role = null;

    public Line(String name, Expression expr, String text) {
        this.name = name;
        this.expr = expr;
        this.text = text;
    }

    public Line(String name, Role role, String text) {
        this.name = name;
        this.role = role;
        this.text = text;
    }
}