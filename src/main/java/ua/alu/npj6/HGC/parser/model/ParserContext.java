package ua.alu.npj6.HGC.parser.model;

import java.util.ArrayList;

import ua.alu.npj6.HGC.Decklist;

public class ParserContext {
    public ArrayList<String> roleNames;
    public ArrayList<Role> roles;

    public ArrayList<String> missingNames;

    public Decklist decklist;

    public ParserContext(Decklist decklist) {
        this.roleNames = new ArrayList<>();
        this.roles = new ArrayList<>();
        this.missingNames = new ArrayList<>();
        this.decklist = decklist;
    }
}