package ua.alu.npj6.HGC.parser.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class Condition {
    Boolean constant = null;

    Restriction restriction = null;

    List<Condition> conditions = null;
    String operation = null;

    public Condition(boolean constant) {
        this.constant = constant;
    }

    public Condition (Restriction restriction) {
        this.restriction = restriction;
    }

    public Condition (List<Condition> conditions, String operation) {
        this.conditions = conditions;
        this.operation = operation;
    }

    public void add(Condition condition) {
        this.conditions.add(condition);
    }

    public String operation() {
        return this.operation;
    }

    // (A | B) x3 -> Ax3 | Ax2&Bx1 | Ax1&Bx2 | Bx3
    // (A | B)!x2 -> A!x2&B!x0 | A!x1&B!x1 | A!x0&B!x2
    private Condition ComplexRoleToOrCondition(ParserContext parserContext) {
        return restriction.complexRole2OrCondition().canonicalForm(parserContext);
    }

    //Use only for &s of simple conditions
    //Counts the minimum draws needed to satisfy the &
    public int restrictionCount() {
        int count = 0;
        for (Condition c : conditions) {
            count += c.restriction.quantity;
        }
        return count;
    }

    public Condition deepCopy() {
        if (this.restriction != null) {
            return new Condition(this.restriction.deepCopy());
        } else if (this.conditions != null) {
            ArrayList<Condition> conditions = new ArrayList<>();
            for (Condition cond : this.conditions) {
                conditions.add(cond.deepCopy());
            }
            return new Condition(conditions, operation);
        } else if (this.constant != null) {
            return new Condition(this.constant);
        } else {
            return null;
        }
    }

    public Condition collapse() {
        if (this.conditions != null) {
            ArrayList<Condition> conditions = new ArrayList<>();

            for (Condition cond : this.conditions) {
                Condition c = cond.collapse();
                //collapse same operations
                if (this.operation.equals(c.operation)) {
                    for (Condition c2 : c.conditions) {
                        conditions.add(c2);
                    }
                } else if (cond.constant != null) {
                    if ((this.operation.equals("&") || this.operation.equals("^&")) && !cond.constant) {
                        //false term inside and operator, result is false
                        return new Condition(false);
                        //true term inside and operator is ignored
                    } else if (this.operation.equals("|") && cond.constant) {
                        //true term inside or operator, result is true
                        return new Condition(true);
                        //false term inside or operator is ignored
                    }

                } else {
                    conditions.add(c);
                }
            }

            if (conditions.size() == 0) {
                if (this.operation.equals("&") || this.operation.equals("^&")) {
                    return new Condition(true);
                } else if (this.operation.equals("|")) {
                    return new Condition(false);
                } else {
                    return null;
                }
            } else if (conditions.size() == 1) {
                return conditions.get(0);
            } else {
                return new Condition(conditions, operation);
            }
        } else {
            return deepCopy();
        }
    }

    public Condition complexRole2OrCondition() {
        if (this.restriction != null) {
            return restriction.complexRole2OrCondition();
        } else if (this.conditions != null) {
            ArrayList<Condition> conditions = new ArrayList<>();

            for (Condition cond : this.conditions) {
                conditions.add(cond.complexRole2OrCondition());
            }

            return new Condition(conditions, operation);
        } else {
            return deepCopy();
        }
    }

    public Condition extractOr() {
        if (this.conditions != null) {
            if ("|".equals(this.operation)) {
                ArrayList<Condition> conditions = new ArrayList<>();
                for (Condition cond : this.conditions) {
                    conditions.add(cond.extractOr());
                }
                //precollapsing after or extraction to avoid nested ors
                return new Condition(conditions, "|").collapse();
            } else {
                //separate in or and notOr conditions
                ArrayList<Condition> orList = new ArrayList<>();
                ArrayList<Condition> notOrList = new ArrayList<>();
                for (Condition cond : this.conditions) {
                    Condition c = cond.extractOr();
                    if ("|".equals(c.operation)) {
                        orList.add(c);
                    } else {
                        notOrList.add(c);
                    }
                }

                //combine all or conditions
                ArrayList<Condition> opConditions = new ArrayList<>();
                opConditions.add(new Condition(new ArrayList<>(), this.operation));
                ArrayList<Condition> newOpConditions;
                //for each or condition
                for (Condition orCond : orList) {
                    newOpConditions = new ArrayList<>();
                    //for each or subcondition
                    for (Condition orSubCond : orCond.conditions) {
                        //copy each operation condition and add a copy of the or subcondition
                        for (Condition opCond : opConditions) {
                            Condition newOpCond = opCond.deepCopy();
                            newOpCond.conditions.add(orSubCond.deepCopy());
                            newOpConditions.add(newOpCond);
                        }
                    }
                    opConditions = newOpConditions;
                }

                //add a copy of all not or conditions to each operation condition
                for (Condition opCond : opConditions) {
                    for (Condition notOrCond : notOrList) {
                        opCond.conditions.add(notOrCond.deepCopy());
                    }
                }

                return new Condition(opConditions, "|");
            }
        } else {
            return deepCopy();
        }
    }

    public Condition flattenAnd() {
        if (this.conditions != null) {
            //flaten all subconditions and collapse
            ArrayList<Condition> conditions = new ArrayList<>();

            for (Condition cond : this.conditions) {
                conditions.add(cond.flattenAnd());
            }

            Condition newCondition = new Condition(conditions, this.operation).collapse();

            if ("|".equals(this.operation) || newCondition.conditions == null) {
                //done
                return newCondition;
            } else if ("&".equals(this.operation)) {
                //compare each new subcondition and merge when possible
                ArrayList<Condition> flattened = new ArrayList<>();
                
                for(Condition cond : newCondition.conditions) {
                    Restriction restriction = cond.restriction;
                    boolean merged = false;
                    
                    for (Condition flatCond : flattened) {
                        Restriction flatRestriction = flatCond.restriction;

                        //if names coincide
                        if (restriction.role.indexes.get(0).equals(flatRestriction.role.indexes.get(0))){
                            if (!restriction.exact && !flatRestriction.exact) {
                                //both GEQ, choose most restrictive
                                flatRestriction.quantity = (
                                    restriction.quantity < flatRestriction.quantity?
                                    flatRestriction.quantity : restriction.quantity
                                );
                            } else if (restriction.exact && flatRestriction.exact) {
                                //both EQ, either equal or incompatible
                                if (restriction.quantity != flatRestriction.quantity) {
                                    return new Condition(false);
                                }
                            } else {
                                //EQ & GEQ
                                Restriction rEQ, rGEQ;
                                if (restriction.exact) {
                                    rEQ = restriction;
                                    rGEQ = flatRestriction;
                                } else {
                                    rEQ = flatRestriction;
                                    rGEQ = restriction;
                                }

                                //if compatible, choose most restrictive
                                if (rEQ.quantity < rGEQ.quantity) {
                                    return new Condition(false);
                                } else {
                                    flatRestriction.exact = true;
                                    flatRestriction.quantity = rEQ.quantity;
                                }
                            }

                            //merged, stop looking
                            merged = true;
                            break;
                        }
                    }

                    //not merged, add a copy
                    if (!merged) {
                        flattened.add(cond.deepCopy());
                    }
                }

                return new Condition(flattened, "&");
            } else if ("^&".equals(this.operation)) {
                ArrayList<Condition> flattened = new ArrayList<>();

                for (Condition cond : newCondition.conditions) {
                    //convert all conditions into andConditions
                    Condition andCond;
                    if (cond.restriction != null) {
                        andCond = new Condition(Arrays.asList(cond), "&");
                    } else {
                        andCond = cond;
                    }

                    //for each subcondition inside the andCondition
                    for (Condition c : andCond.conditions) {
                        Restriction r = c.restriction;
                        boolean merged = false;

                        //try to merge with each flat condition
                        for(Condition flatCond : flattened) {
                            Restriction flatR = flatCond.restriction;
                            if (r.role.indexes.get(0).equals(flatR.role.indexes.get(0))) {
                                if (!r.exact && !flatR.exact) {
                                    //GEQ ^& GEQ, both satisfied exclusively requires at least the sum of the mins
                                    flatR.quantity += r.quantity;
                                } else {
                                    //EQ and XAnd are not compatible
                                    return new Condition(false);
                                }

                                //merged, stop looking
                                merged = true;
                                break;
                            }

                        }

                        //not merged, add a copy
                        if (!merged) {
                            flattened.add(c.deepCopy());
                        }

                        
                    }

                    
                }

                return new Condition(flattened, "&");
            } else {
                return null;
            }
            
        } else {
            return deepCopy();
        }
    }

    public boolean isMoreOrEquallyRestrictiveThan(Condition c) {
        if (this.restriction != null && c.restriction != null) {
            return this.restriction.isMoreOrEquallyRestrictiveThan(c.restriction);
        } else if (this.conditions != null && c.conditions != null && this.operation.equals(c.operation)) {
            for (Condition c2 : c.conditions) {
                boolean found = false;
                for (Condition c1 : this.conditions) {
                    if (c1.isMoreOrEquallyRestrictiveThan(c2)) {
                        found = true;
                    }
                }
                if (!found) {
                    return false;
                }
            }
            return true;
        } else if (this.constant != null && c.constant != null) {
            return this.constant.equals(c.constant);
        } else {
            return false;
        }
    }
    
    public Condition purgeOr() {
        if (this.conditions != null && "|".equals(this.operation)) {
            List<Condition> conditions = this.conditions;
            List<Condition> newConditions;
            boolean changed = true;

            while (changed) {
                changed = false;

                for (Condition cond1 : conditions) {
                    newConditions = new ArrayList<>();
                    newConditions.add(cond1.deepCopy());

                    for (Condition cond2 : conditions) {
                        if (cond1 != cond2) {
                            if (cond2.isMoreOrEquallyRestrictiveThan(cond1)) {
                                changed = true;
                            } else {
                                newConditions.add(cond2.deepCopy());
                            }
                        }
                    }

                    if (changed) {
                        conditions = newConditions;
                        break;
                    }
                }
            }



            return new Condition(conditions, "|");
        } else {
            return deepCopy();
        }
    }

    public Condition canonicalForm() {
        Condition current = collapse();
        current = current.complexRole2OrCondition();
        current = current.collapse();
        current = current.extractOr();
        current = current.collapse();
        current = current.flattenAnd(); //collapses inside, probably no need for another one
        current = current.purgeOr();
        current = current.collapse();
        return current;
    }

    public Condition canonicalForm(ParserContext parserContext) {
        if (this.restriction != null) {
            //Simple condition
            if (restriction.role.indexes.size() == 0) {
                //Empty role
                if (restriction.quantity == 0) {
                    return new Condition(true);
                } else {
                    return new Condition(false);
                }
            } else if (restriction.role.indexes.size() == 1) {
                int idx = restriction.role.indexes.get(0);
                //Simple role
                if (idx < 0) {
                    if (restriction.quantity == 0) {
                        return new Condition(true);
                    } else {
                        return new Condition(false);
                    }
                }else if (!restriction.exact && restriction.quantity == 0) {
                    return new Condition(true);
                } else if (parserContext.decklist.quantities[idx] < restriction.quantity) {
                    return new Condition(false);
                } else if (parserContext.hand < restriction.quantity) {
                    return new Condition(false);
                } else {
                    return new Condition(restriction.canonicalForm(parserContext));
                }
            } else {
                //Complex role
                if (!restriction.exact && restriction.quantity == 0) {
                    return new Condition(true);
                } else {
                    return ComplexRoleToOrCondition(parserContext);
                }
            }
        } else if (this.conditions != null) {
            //Complex conditions
            if (this.conditions.size() == 0) {
                //No subconditions
                return new Condition(false);
            } else if (this.conditions.size() == 1) {
                //Single subcondition
                return this.conditions.get(0).canonicalForm(parserContext);
            } else {
                //Multiple subconditions
                ArrayList<Condition> conditions = new ArrayList<>();

                for (Condition cond : this.conditions) {
                    Condition c = cond.canonicalForm(parserContext);

                    if (c.constant != null) {
                        if (operation.equals("&") || operation.equals("^&")) {
                            if (!c.constant) {
                                return new Condition(false);
                            } // true & is ignored
                        } else if (operation.equals("|")) {
                            if (c.constant) {
                                return new Condition(true);
                            } // false | is ignored
                        } // there should be no other operations
                    } else {
                        //collapse same operations
                        if (operation.equals(c.operation)) {
                            for (Condition c2 : c.conditions) {
                                conditions.add(c2);
                            }
                        } else {
                            conditions.add(c);
                        }
                    }
                }

                return new Condition(conditions, operation);
            }
        }  else if (this.constant != null) {
            //Constant condition
            return new Condition(this.constant);
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        String out = "";

        if (restriction != null) {
            out += restriction.toString().replaceAll("\n", "\n\t");
        }
        if (conditions != null) {
            out += "Condition "+operation;
            for (Condition cond : conditions) {
                out += "\n\t" + cond.toString().replaceAll("\n", "\n\t");
            }
        }
        if (constant != null) {
            out += constant.toString();
        }


        return out;
    }
}