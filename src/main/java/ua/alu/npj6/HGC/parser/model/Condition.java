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

    public String operation() {
        return this.operation;
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

    public Expression condition2Expression(int draws) {
        if (this.constant != null) {
            return new Expression(this.constant);
        } else if (this.conditions != null) {
            ArrayList<Expression> expressions = new ArrayList<>();
            for (Condition c : this.conditions) {
                expressions.add(c.condition2Expression(draws));
            }
            return new Expression(expressions, this.operation);
        } else if (this.restriction != null) {
            return new Expression(this.deepCopy(), draws);
        } else {
            return null;
        }
    }

    public Condition restrictMaximum(int draws) {
        if ("|".equals(this.operation)) {
            ArrayList<Condition> conditions = new ArrayList<>();
            for (Condition c : this.conditions) {
                conditions.add(c.restrictMaximum(draws));
            }
            return new Condition(conditions, "|").collapse();
        } else if ("&".equals(this.operation) && draws < this.restrictionCount()) {
                return new Condition(false);
        } else {
            return this.deepCopy();
        }
    }

    public boolean implies(Condition that) {
        if (this.constant != null && that.constant != null) {
            return this.constant.equals(that.constant);
        } else if (this.constant != null || that.constant != null) {
            return false;
        } else if (this.restriction != null && that.restriction != null) {
            return this.restriction.implies(that.restriction);
        } else if ("&".equals(this.operation) && that.restriction != null) {
            //if any element of this (&) implies that (simple), this implies that
            for (Condition thisC : this.conditions) {
                if (thisC.implies(that)) {
                    return true;
                }
            }
            return false;
        } else if (!"|".equals(this.operation) && "&".equals(that.operation)) {
            //if all elements of that (&) are implied by this (&/simple), this implies that
            for (Condition thatC : that.conditions) {
                if(!this.implies(thatC)) {
                    return false;
                }                
            }
            return true;
        } else if (!"|".equals(this.operation) && "|".equals(that.operation)) {
            //if any element of that (|) is implied by this (&/simple), this implies that
            for (Condition thatC : that.conditions) {
                if (this.implies(thatC)) {
                    return true;
                }
            }
            return false;
        } else if ("|".equals(this.operation)) {
            //if all elements of this (|) imply that (|/&/simple), this implies that
            for (Condition thisC : this.conditions) {
                if (!thisC.implies(that)) {
                    return false;
                }
            }
            return true;
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
                            if (cond2.implies(cond1)) {
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

    //apply only after canonical form
    public Condition optimize(ParserContext parserContext) {
        if (this.conditions != null) {
            ArrayList<Condition> conditions = new ArrayList<>();
            for(Condition c : this.conditions) {
                conditions.add(c.optimize(parserContext));
            }
            return new Condition(conditions, this.operation);
        } else if (this.restriction != null) {
            int idx = this.restriction.role.indexes.get(0);
            if (idx < 0) {
                if (this.restriction.exact && this.restriction.quantity == 0) {
                    return new Condition(true); //exactly 0 of non existent? no prob bob.
                } else {
                    return new Condition(false);
                }
            } else if (parserContext.decklist.quantities[idx] < this.restriction.quantity) {
                return new Condition(false);
            } else {
                return deepCopy();
            }
        } else {
            return deepCopy();
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