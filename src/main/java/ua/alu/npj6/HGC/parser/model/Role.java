package ua.alu.npj6.HGC.parser.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class Role {
    public List<Integer> indexes;

    public Role(int idx) {
        this.indexes = Arrays.asList(idx);
    }

    public Role(List<Integer> indexes) {
        this.indexes = new ArrayList<>();
        for (Integer idx : indexes) {
            this.indexes.add(idx);
        }
        this.indexes.sort(Comparator.naturalOrder());
    }

    public Role deepCopy() {
        ArrayList<Integer> indexes = new ArrayList<>();

        for (Integer i : this.indexes) {
            indexes.add(i);
        }

        return new Role(indexes);
    }

    Role common(Role r2) {
        ArrayList<Integer> common = new ArrayList<>();
        for (Integer idx1 : this.indexes) {
            if (r2.indexes.contains(idx1)) {
                common.add(idx1);
            }
        }
        return new Role(common);
    }

    Role subtract(Role r2) {
        ArrayList<Integer> subtract = new ArrayList<>();
        for (Integer idx1 : this.indexes) {
            if (!r2.indexes.contains(idx1)) {
                subtract.add(idx1);
            }
        }
        return new Role(subtract);
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

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Role))
            return false;
        Role other = (Role) o;
        return this.indexes.equals(other.indexes);
    }
}