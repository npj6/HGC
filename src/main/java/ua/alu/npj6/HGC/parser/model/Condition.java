package ua.alu.npj6.HGC.parser.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Collections;

import ua.alu.npj6.HGC.utils.Timer;

public class Condition {
    Boolean constant = null;

    Restriction restriction = null;

    ArrayList<Condition> conditions = null;
    String operation = null;

    //every subcondition is simple
    boolean pure = true;

    Restriction[] baseAndShortForm(List<Integer> relevantIndexes) {
        Restriction[] shortForms = new Restriction[relevantIndexes.size()];
        for (int i=0; i<shortForms.length; i++) {
            shortForms[i] = new Restriction(null, false, -1);
        }
        return shortForms;
    }

    void addRestriction(List<Integer> relevantIndexes, Restriction[] shortForms, Restriction r) {
        int idx = relevantIndexes.indexOf(r.role.indexes.get(0));
        shortForms[idx] = r.shortForm();
    }

    //use only after canonicalForm
    Restriction[][] shortForm(List<Integer> relevantIndexes) {
        if (this.restriction != null) {
            Restriction[] shortForms = baseAndShortForm(relevantIndexes);
            addRestriction(relevantIndexes, shortForms, this.restriction);
            return new Restriction[][]{shortForms};
        } else if ("&".equals(this.operation)) {
            Restriction[] shortForms = baseAndShortForm(relevantIndexes);
            for (Condition cond : this.conditions) {
                addRestriction(relevantIndexes, shortForms, cond.restriction);
            }
            return new Restriction[][]{shortForms};
        } else if ("|".equals(this.operation)) {
            Restriction[][] shortForms = new Restriction[this.conditions.size()][];
            for (int i=0; i<this.conditions.size(); i++) {
                shortForms[i] = this.conditions.get(i).shortForm(relevantIndexes)[0];
            }
            return shortForms;
        } else {
            return null;
        }

    }

    ArrayList<Role> splitIntoGroups(ArrayList<Role> groups, Role role) {
        for(int i=0; i<groups.size(); i++) {
            Role r = groups.get(i);
            Role common = r.common(role);
            //ignore independent roles
            if (!common.indexes.isEmpty()) {
                if (common.indexes.size() < r.indexes.size()) {
                    //group r needs to be broken up
                    groups.remove(i);
                    groups.add(common);
                    groups.add(r.subtract(common));
                    return splitIntoGroups(groups, role);
                } else if (common.indexes.size() < role.indexes.size()) {
                    //role needs to be broken up
                    role = role.subtract(common);
                } else {
                    //match found, grouping is ok
                    return groups;
                }
            }
        }

        //if code reaches this point, all idx in role are absent from any group
        groups.add(role.deepCopy());
        return groups;
    }

    ArrayList<Role> getRoleGroups(ArrayList<Role> groups) {
        if (this.restriction != null) {
            groups = splitIntoGroups(groups, this.restriction.role);
        } else if (this.conditions != null) {
            for (Condition c : this.conditions) {
                groups = c.getRoleGroups(groups);
            }
        }
        return groups;
    }

    Condition groupRoles(ArrayList<Role> groups) {
        if (this.restriction != null) {
            ArrayList<Integer> newRoles = new ArrayList<>();
            for (int i=0; i<groups.size(); i++) {
                //we already split the groups, if the first element is contained all elements are
                if (this.restriction.role.indexes.contains(groups.get(i).indexes.get(0))) {
                    newRoles.add(i);
                }
            }
            this.restriction.role = new Role(newRoles);
        } else if (this.conditions != null) {
            for(int i=0; i<this.conditions.size(); i++) {
                this.conditions.set(i, this.conditions.get(i).groupRoles(groups));
            }
        }
        return this;
    }

    //use only after canonical form
    List<Integer> relevantIndexes() {
        if (this.restriction != null) {
            return Arrays.asList(this.restriction.role.indexes.get(0));
        } else if (this.conditions != null) {
            ArrayList<Integer> relevantIndexes = new ArrayList<>();

            for (Condition cond : this.conditions) {
                for (Integer idx : cond.relevantIndexes()) {
                    if (!relevantIndexes.contains(idx)) {
                        relevantIndexes.add(idx);
                    }
                }
            }

            return relevantIndexes;
        } else {
            return new ArrayList<>();
        }
    }

    public Condition(boolean constant) {
        this.constant = constant;
    }

    public Condition (Restriction restriction) {
        this.restriction = restriction;
    }

    public Condition (List<Condition> conditions, String operation) {
        ArrayList<Condition> conds = new ArrayList<>();
        for (Condition c: conditions) {
            if (c.constant != null) {
                if (!(c.constant ^ "|".equals(operation))) {
                    // true constant absorbs ors, false constant absorbs ands
                    this.constant = c.constant;
                    return;
                }
                // otherwise constant is ignored
            } else {
                conds.add(c);
                if (pure && c.conditions != null) {
                    pure = false;
                }
            }
        }
        this.operation = operation;
        this.conditions = conds;
    }

    boolean isPure() {
        if (conditions != null) {
            for (Condition c : conditions) {
                if (c.conditions != null) {
                    return false;
                }
            }
        }
        return true;
    }

    public String operation() {
        return this.operation;
    }

    public Condition orderByRestriction() {
        if (this.conditions != null) {
            //c2.size - c1.size
            this.conditions.sort( (Condition c1, Condition c2) -> {
                if (c1.conditions != null && c2.conditions != null) {
                    return c2.conditions.size() - c1.conditions.size();
                } else if (c1.restriction != null && c2.restriction != null) {
                    if (c1.restriction.exact == c2.restriction.exact) {
                        return c1.restriction.quantity - c2.restriction.quantity;
                    } else {
                        return (c1.restriction.exact ? 1 : -1);
                    }
                } else {
                    return (c1.conditions != null ? -1 : 1);
                }
            });
        }
        return this;
    }

    //only recursive function in the node pipeline
    public Condition canonicalFormNode() {
        if (restriction != null) {
            Condition c = restriction.complexRole2OrCondition();
            
            if (c.conditions != null) {
                c = c.canonicalFormNode();
            }

            return c;
        } else if (conditions != null) {
            ArrayList<Condition> newConditions = new ArrayList<>();
            for (Condition c : conditions) {
                newConditions.add(c.canonicalFormNode());
            }
            
            //deletes constants
            Condition current = new Condition(newConditions, operation).collapseNode();

            //leaf nodes
            if (current.constant != null || current.restriction != null) {
                return current;
            }
            
            current.orderByRestriction();

            current = current.extractOrNode();

            return current;

        } else {
            return this;
        }
    }

    //assumes every subcondition is already in canonical form
    public Condition collapseNode() {
        if (this.conditions != null && !this.pure) {
            //only iterates through original subconditions
            int total = this.conditions.size();
            for (int i=0; i<total; i++) {
                Condition cond = this.conditions.get(i);
                if (operation.equals(cond.operation)) {
                    //collapse same operations
                    this.conditions.remove(i);
                    total--; i--;
                    //if collapsing an and, start by looking for matches and merging
                    for(int j=0; j<cond.conditions.size(); j++) {
                        Condition c2 = cond.conditions.get(j);
                        if ("&".equals(this.operation)) {
                            Condition c1 = this.combine(c2, "&");
                            if (Boolean.FALSE.equals(c1.constant)) {
                                return c1;
                            }
                        } else {
                            this.conditions.add(c2);
                        }
                    }
                }
            }

            //consider the results after collapsing
            if (conditions.size() == 0) {
                return new Condition(!"|".equals(operation));
            } else if (conditions.size() == 1) {
                return conditions.get(0);
            } else {
                this.pure = this.isPure();
                return this;
            }
        } else {
            //do nothing
            return this;
        }

    }

    //asumes to be a pure & or a simple cond
    //does not modify cond, which is asumed a simple or pure & condition in canonical form
    public Condition combine(Condition cond, String operation) {
        //for each simple condition to combine
        int total = (cond.restriction != null ? 1 : cond.conditions.size());
        for (int i=0; i<total; i++) {
            Condition c1 = (cond.restriction != null ? cond : cond.conditions.get(i));
            //look for its role inside this
            boolean found = false;
            int total2 = (this.restriction != null ? 1 : this.conditions.size());
            for (int j=0; j<total2; j++) {
                Condition c2 = (this.restriction != null ? this : this.conditions.get(j));
                if (c2.restriction != null && c1.restriction.role.equals(c2.restriction.role)){
                    //if found, try to merge them
                    found = true;
                    Restriction r;
                    if ("&".equals(operation)){
                        r = c1.restriction.combineAnd(c2.restriction);
                    } else {
                        r = c1.restriction.combineXAnd(c2.restriction);
                    }
                    
                    if (r == null) {
                        //cannot be merged, this becomes false
                        return new Condition(false);
                    } else if (this.conditions != null) {
                        this.conditions.set(j, new Condition(r));
                    } else {
                        this.restriction = r;
                    }
                    //stop looking
                    break;
                }
            }

            if (!found) {
                //if not found, add the condition to this
                if (this.conditions != null) {
                    this.conditions.add(c1);
                } else {
                    this.conditions = new ArrayList<>();
                    this.operation = "&";
                    this.conditions.add(new Condition(this.restriction));
                    this.conditions.add(c1);
                    this.pure = true;
                    this.restriction = null;
                }
            }
        }

        return this;
    }


    public Condition removeDepsOr() {
        if (this.conditions != null && "|".equals(this.operation)) {

            int total = this.conditions.size();
            for (int i=0; i<total; i++) {
                for (int j=0; j<total; j++) {
                    if (i != j) {
                        if (this.conditions.get(j).implies(this.conditions.get(i))) {
                            this.conditions.remove(j);
                            total--;
                            if(j < i) {
                                i--;
                            }
                            j--;
                        }
                    }
                }
            }
            
            if (this.conditions.size() == 0) {
                return new Condition(true); //should not happen
            } else if (this.conditions.size() == 1) {
                return this.conditions.get(0);
            } else {
                this.pure = this.isPure();
                return this;
            }
        } else {
            return this;
        }

    }

    public static Timer impliesTimer = null;
    public static Timer depsTimer = null;
    //assumes condition has been collapsed (+ collapsed assumptions), may modify this
    public Condition extractOrNode() {
        if (this.conditions == null || this.pure && "&".equals(this.operation)) {
            // do nothing to simple conditions and pure & conditions
            return this;
        } else if ("|".equals(this.operation)) {
            //check dependancy in |
            return depsTimer.timeAcc(() -> this.removeDepsOr());
        } else {
            //every subcondition is simple, an | or a pure &
            
            //combine all simple and pure & conditions
        
            Condition pureAnd = new Condition(Collections.emptyList(), "&");
            for (Condition c : this.conditions) {
                if (!"|".equals(c.operation)) {
                    pureAnd = pureAnd.combine(c, this.operation);
                    if (Boolean.FALSE.equals(pureAnd.constant)) {
                        //if pureAnd becomes false, will always be false
                        break;
                    }
                }
            }

            if (pureAnd.conditions.size() == 0) {
                pureAnd = new Condition(false);
            } else if (pureAnd.conditions.size() == 1) {
                pureAnd = pureAnd.conditions.get(0);
            }
            
            //extract the ors
            ArrayList<Condition> conditions = new ArrayList<>();
            conditions.add(pureAnd);

            for (Condition c : this.conditions) {
                if ("|".equals(c.operation)) {
                    //only iterates through original conditions for each | found
                    int total = conditions.size();
                    
                    while (0 < total) {
                        total--;
                        pureAnd = conditions.get(0);
                        conditions.remove(0);

                        //tries to combine each subSubCond with the pureAnds
                        for (Condition subSubCond : c.conditions) {
                            
                            //ignores false pureAnds, instead tries to add the subSubCond
                            if (!Boolean.FALSE.equals(pureAnd.constant)) {
                                subSubCond = subSubCond.deepCopy().combine(pureAnd, this.operation);
                                if (subSubCond.conditions != null && subSubCond.conditions.size() == 1) {
                                    subSubCond = subSubCond.conditions.get(0);
                                }
                            }
                            
                            //ignores false subSubConds (from having combined)
                            if (!Boolean.FALSE.equals(subSubCond.constant)) {
                                //check independance from the others subSubConds
                                boolean independent = true;
                                for (int i=total; i<conditions.size(); i++) {
                                    Condition other = conditions.get(i);
                                    Condition subSubCond2 = subSubCond;
                                    if (impliesTimer.timeAcc(() -> other.implies(subSubCond2))) {
                                        //remove redundant conditions
                                        conditions.remove(i);
                                        i--;
                                    } else if (impliesTimer.timeAcc(() -> subSubCond2.implies(other))) {
                                        independent = false;
                                        break;
                                    }
                                }

                                //only add independent conditions
                                if (independent) {
                                    conditions.add(subSubCond);
                                }
                            }
                        }
                    }
                }
            }

            if (conditions.size() == 0) {
                return new Condition(false);
            } else if (conditions.size() == 1) {
                return conditions.get(0);
            } else {
                return new Condition(conditions, "|");
            }
        }
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
        if (conditions != null) {
            int total = conditions.size();
            //for each subcondition
            for (int i=0; i<total; i++) {
                Condition cond = conditions.get(i);
                if (cond.constant != null) {
                    //remove or return constants
                    if ("|".equals(operation)) {
                        //or condition
                        if (cond.constant) {
                            return new Condition(true);
                        } else {
                            conditions.remove(i);
                            total--; i--;
                        }
                    } else {
                        //and xand condition
                        if (cond.constant) {
                            conditions.remove(i);
                            total--; i--;
                        } else {
                            return new Condition(false);
                        }
                    }
                } else if (operation.equals(cond.operation)) {
                    //collapse same operations
                    conditions.remove(i);
                    total--; i--;
                    for(int j=0; j<cond.conditions.size(); j++) {
                        conditions.add(cond.conditions.get(j));
                        total++;
                    }
                } else {
                    //collapse, reevaluate if there's a change
                    conditions.set(i, cond.collapse());
                    if (cond != conditions.get(i)) {
                        i--;
                    }
                }
            }

            //consider the results after collapsing
            if (conditions.size() == 0) {
                return new Condition(!"|".equals(operation));
            } else if (conditions.size() == 1) {
                return conditions.get(0);
            } else {
                return this;
            }
        } else {
            //do nothing
            return this;
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

            int total = this.conditions.size();
            for (int i=0; i<total; i++) {
                for (int j=0; j<total; j++) {
                    if (i != j) {
                        if (this.conditions.get(j).implies(this.conditions.get(i))) {
                            this.conditions.remove(j);
                            total--;
                            if(j < i) {
                                i--;
                            }
                            j--;
                        }
                    }
                }
            }


            return this;
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
            if (parserContext.roleGroups.get(idx).indexes.stream().allMatch((e) -> e < 0)) {
                if (this.restriction.exact && this.restriction.quantity == 0) {
                    return new Condition(true); //exactly 0 of non existent? no prob bob.
                } else {
                    return new Condition(false);
                }
            } else if (
                parserContext.roleGroups.get(idx).indexes.stream()
                    .mapToInt(r -> parserContext.decklist.quantities[r])
                    .reduce(0, Integer::sum) < this.restriction.quantity
                ) {
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
            out += "Condition "+operation+" "+conditions.size();
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