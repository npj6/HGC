package ua.alu.npj6.HGC.parser.model;

import java.util.ArrayList;

import ua.alu.npj6.HGC.Decklist;

public class ParserContext {
    public ArrayList<String> roleNames;
    public ArrayList<Role> roles;

    public ArrayList<String> condNames;
    public ArrayList<Condition> conditions;

    public ArrayList<String> exprNames;
    public ArrayList<Expression> expressions;

    public ArrayList<String> missingRoleNames;
    public ArrayList<String> missingCondNames;
    public ArrayList<String> missingExprNames;

    public Decklist decklist;

    public ParserContext(Decklist decklist) {
        this.roleNames = new ArrayList<>();
        this.roles = new ArrayList<>();
        
        this.condNames = new ArrayList<>();
        this.conditions = new ArrayList<>();

        this.exprNames = new ArrayList<>();
        this.expressions = new ArrayList<>();

        this.missingRoleNames = new ArrayList<>();
        this.missingCondNames = new ArrayList<>();
        this.missingExprNames = new ArrayList<>();
        
        this.decklist = decklist;
    }
}