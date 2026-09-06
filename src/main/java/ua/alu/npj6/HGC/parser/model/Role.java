package ua.alu.npj6.HGC.parser.model;

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

    public boolean implies(Role that) {
        for(Integer i2 : that.indexes) {
            boolean found = false;
            for (Integer i1 : this.indexes) {
                if (i2.equals(i1)) {
                    found = true;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return indexes.toString();
    }
}