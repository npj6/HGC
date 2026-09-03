package ua.alu.npj6.parser.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class Role {
    public List<Integer> indexes;

    public Role(int idx) {
        this.indexes = Arrays.asList(idx);
    }

    public Role(List<Integer> indexes) {
        this.indexes = indexes;
    }

    public Role deepCopy() {
        ArrayList<Integer> indexes = new ArrayList<>();

        for (Integer i : this.indexes) {
            indexes.add(i);
        }

        return new Role(indexes);
    }

    //the more subroles, the less restrictive
    public boolean isMoreOrEquallyRestrictiveThan(Role r) {
        for(Integer i1 : this.indexes) {
            boolean found = false;
            for (Integer i2 : r.indexes) {
                if (i1.equals(i2)) {
                    found = true;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    public Role canonicalForm(ParserContext parserContext) {
        return deepCopy();
    }

    @Override
    public String toString() {
        return indexes.toString();
    }
}