package ua.alu;

class Card{
    public final String name;
    public final int number;

    public Card (String name, int number) {
        this.name = name;
        this.number = number;
    }

    @Override
    public String toString() {
        return name+" ("+Integer.toString(number)+")";
    }
}