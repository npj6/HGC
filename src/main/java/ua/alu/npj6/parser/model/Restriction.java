package ua.alu.npj6.parser.model;

public class Restriction {
    Role role;
    boolean exact;
    int quantity;

    public Restriction(Role role, boolean exact, int quantity) {
        this.role = role;
        this.exact = exact;
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return role.toString()+(exact?"!x":"x")+Integer.toString(quantity);
    }
}