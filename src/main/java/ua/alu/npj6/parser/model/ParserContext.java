package ua.alu.npj6.parser.model;

import java.util.ArrayList;

import ua.alu.npj6.Decklist;

public class ParserContext {
    public ArrayList<String> roleNames;
    public ArrayList<Role> roles;

    public ArrayList<String> missingNames;

    public Decklist decklist;

    int hand;

    public ParserContext(Decklist decklist, int hand) {
        this.roleNames = new ArrayList<>();
        this.roles = new ArrayList<>();
        this.missingNames = new ArrayList<>();
        this.decklist = decklist;
        this.hand = hand;
    }
}