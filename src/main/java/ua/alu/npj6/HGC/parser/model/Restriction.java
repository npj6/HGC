package ua.alu.npj6.HGC.parser.model;

import java.util.ArrayList;

public class Restriction {
    public Role role;
    public boolean exact;
    public int quantity;

    //does not modify
    public Restriction combineAnd(Restriction that) {
        if (this.role.equals(that.role)) {
            if (!this.exact && !this.exact) {
                //both GEQ, choose most restrictive
                return (this.quantity < that.quantity ? that : this);
            } else if (this.exact && that.exact) {
                //both EQ, either equal or incompatible
                if (this.quantity == that.quantity) {
                    return this;
                } else {
                    return null;
                }
            } else {
                //EQ & GEQ
                Restriction rEQ, rGEQ;
                if (this.exact) {
                    rEQ = this;
                    rGEQ = that;
                } else {
                    rEQ = that;
                    rGEQ = this;
                }

                //if compatible, choose most restrictive
                if (rEQ.quantity < rGEQ.quantity) {
                    return null;
                } else {
                    return rEQ;
                }
            }

        } else {
            return null;
        }   
    }

    //does not modify, may create a new Restriction
    public Restriction combineXAnd(Restriction that) {
        if (this.role.equals(that.role)) {
            if (!this.exact && !that.exact) {
                //GEQ ^& GEQ, both satisfied exclusively requires at least the sum of the mins
                return new Restriction(this.role, false, this.quantity+that.quantity);
            } else {
                //EQ and XAnd are not compatible
                return null;
            }
        } else {
            return null;
        }
    }


    public Restriction shortForm() {
        Restriction out = deepCopy();
        out.role = null;
        return out;
    }

    public Restriction(Role role, boolean exact, int quantity) {
        this.role = role;
        this.exact = exact;
        this.quantity = quantity;
    }

    public boolean implies(Restriction that) {
        if (this.role.implies(that.role)) { //efectively EQ
            if (!this.exact && !that.exact) {
                return that.quantity <= this.quantity;
            } else if (this.exact && that.exact) {
                return that.quantity == this.quantity && that.role.implies(this.role);
            } else {
                if (!this.exact) {
                    return false;
                } else {
                    return that.quantity <= this.quantity;
                }
            }
        } else {
            return false;
        }
    }

    // (A | B) x3 -> Ax3 | Ax2&Bx1 | Ax1&Bx2 | Bx3
    // (A | B)!x2 -> A!x2&B!x0 | A!x1&B!x1 | A!x0&B!x2
    public Condition complexRole2OrCondition() {
        if (quantity == 0 && !exact) {
            //Useless restriction
            return new Condition(true);
        } else if (this.role.indexes.size() < 2) {
            //Simple role
            return new Condition(deepCopy());
        } else {
            //Complex role
            ArrayList<Condition> conditions = new ArrayList<>();
            conditions.add(new Condition(new ArrayList<>(), "&"));
            ArrayList<Condition> newConditions;

            //for each name in the complex role
            for (int i=0; i < role.indexes.size(); i++) {
                int idx = role.indexes.get(i);

                if(i < role.indexes.size()-1) {
                    newConditions = new ArrayList<>();

                    //generate all combinations under the restriction up to the last name
                    for(Condition cond : conditions) {
                        int count = cond.restrictionCount();
                        for (int q = 0; q <= (quantity - count); q++) {
                            Condition newC = cond.deepCopy();
                            if (q != 0 || exact) {
                                newC.conditions.add(new Condition(new Restriction(new Role(idx), exact, q)));
                            }
                            newConditions.add(newC);
                        }
                    }

                    conditions = newConditions;
                } else {
                    //fulfill the restriction with the last name
                    for (Condition cond : conditions) {
                        int count = cond.restrictionCount();
                        if (quantity - count != 0 || exact) {
                            cond.conditions.add(new Condition(new Restriction(new Role(idx), exact, quantity-count)));
                        }
                    }
                }
            }
            for (int i=0; i<conditions.size(); i++) {
                if (conditions.get(i).conditions.size() == 1) {
                    conditions.set(i, conditions.get(i).conditions.get(0));
                }
            }
            
            return new Condition(conditions, "|");
        }
    }

    public Restriction deepCopy() {
        return new Restriction(role.deepCopy(), exact, quantity);
    }

    @Override
    public String toString() {
        return (role == null ? "" : role.toString())+(exact?"!x":"x")+Integer.toString(quantity);
    }
}