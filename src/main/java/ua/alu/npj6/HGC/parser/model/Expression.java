package ua.alu.npj6.HGC.parser.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

import java.util.Comparator;

import ua.alu.npj6.HGC.Decklist;

public class Expression {
    Condition condition = null;

    List<Expression> expressions = null;
    String operation = null;

    int draws;

    public int getDraws() { return draws; }

    Boolean constant = null;

    Restriction[][][][] shortForm(List<Integer> relevantIndexes) {
        if (this.condition != null) {
            Restriction[][][] shortForms = new Restriction[][][]{this.condition.shortForm(relevantIndexes)};
            return new Restriction[][][][]{shortForms};
        } else if ("&".equals(this.operation)) {
            Restriction[][][] shortForms = new Restriction[this.expressions.size()][][];
            for (int i=0; i<this.expressions.size(); i++) {
                shortForms[i] = this.expressions.get(i).condition.shortForm(relevantIndexes);
            }
            return new Restriction[][][][]{shortForms};
        } else if ("|".equals(this.operation)) {
            Restriction[][][][] shortForms = new Restriction[this.expressions.size()][][][];
            for (int i=0; i<this.expressions.size(); i++) {
                shortForms[i] = this.expressions.get(i).shortForm(relevantIndexes)[0];
            }
            return shortForms;
        } else {
            return null;
        }
    }

    int[][][] list2Array(List<List<int[]>> list) {
        int[][][] out = new int[list.size()][][];
        for (int i=0; i<list.size(); i++) {
            out[i] = new int[list.get(i).size()][];
            for (int j=0; j<list.get(i).size(); j++) {
                out[i][j] = list.get(i).get(j);
            }
        }
        return out;
    }

    //assumes root expression
    int[][][] groupByDraws() {
        if (this.condition != null) {
            return new int[][][]{{{0, 0}}};
        } else if ("&".equals(this.operation)) {
            List<List<int[]>> count = new ArrayList<>();

            //for each possible draws value
            for (int d=0; d<=this.draws; d++) {
                //save the shortForm position of matching expressions
                List<int[]> c = new ArrayList<>(); 
                for (int expr=0; expr<this.expressions.size(); expr++) {
                    if (this.expressions.get(expr).draws == d) {
                        c.add(new int[]{0, expr});
                    }
                }

                //only add the list of d draws expressions if not empty
                if (!c.isEmpty()) {
                    count.add(c);
                }
            }

            return list2Array(count);

        } else if ("|".equals(this.operation)) {
            List<List<int[]>> count = new ArrayList<>();
            
            //for each possible draws value
            for (int d=0; d<=this.draws; d++) {
                //save the shortForm position of matching expressions
                List<int[]> c = new ArrayList<>();
                for (int expr1=0; expr1<this.expressions.size(); expr1++) {
                    Expression e1 = this.expressions.get(expr1);
                    if (e1.condition != null) {
                        //simple subexpression
                        if (e1.draws == d) {
                            c.add(new int[]{expr1, 0});
                        }
                    } else if ("&".equals(e1.operation)) {
                        //and subexpression
                        for (int expr2=0; expr2<e1.expressions.size(); expr2++) {
                            Expression e2 = e1.expressions.get(expr2);
                            if (e2.draws == d) {
                                c.add(new int[]{expr1, expr2});
                            }
                        }
                    }
                }

                //only add the list of d draws expressions if not empty
                if(!c.isEmpty()) {
                    count.add(c);
                }
            }

            return list2Array(count);
        } else {
            return null;
        }

    }

    //use only after canonical form
    public Supplier<BiPredicate<Decklist, int[]>> getPredicate(Decklist decklist) {
        if (this.constant != null) {
            return () -> (Decklist dList, int[] hand) -> this.constant;
        } else {
            List<Integer> relevantIndexes = this.relevantIndexes();
            relevantIndexes.sort(Comparator.naturalOrder());
            
            //returns the short array idx for each card, -1 means not relevant
            int indexes[] = new int[decklist.names.length];
            for(int i=0; i<indexes.length; i++) {
                int idx = relevantIndexes.indexOf(i);
                indexes[i] = idx;
            }

            //Array (Expr |) of array (Expr &) of array (Cond |) of array (Cond &) of clean restrictions
            Restriction[][][][] shortForms = this.shortForm(relevantIndexes);

            //Arrays of shortForm positions grouped by number of draws
            int[][][] drawsGroups = this.groupByDraws();

            //Array with the ammounts of draws in each group
            int[] drawsArray = new int[drawsGroups.length];
            for(int i=0; i<drawsGroups.length; i++) {
                if (this.condition != null) {
                    drawsArray[i] = this.draws;
                } else if ("&".equals(this.operation)) {
                    drawsArray[i] = this.expressions.get(drawsGroups[i][0][1]).draws;
                } else if ("|".equals(this.operation)) {
                    Expression subExpr = this.expressions.get(drawsGroups[i][0][0]);
                    if (subExpr.condition != null) {
                        drawsArray[i] = subExpr.draws;
                    } else if ("&".equals(subExpr.operation)) {
                        drawsArray[i] = subExpr.expressions.get(drawsGroups[i][0][1]).draws;
                    }
                }
            }

            //Research memory alignment to avoid cache misses

            return () -> {
                //count of relevant indexes in the hand, must be reset to 0
                int[] handQuantities = new int[shortForms[0][0][0].length];
                //keeps track of which and expressions have been falsed, must be reset to true
                boolean[] andExprIs = new boolean[shortForms.length];

                return (Decklist dList, int[] hand) -> {
                    //reset hand state
                    for (int i=0; i<handQuantities.length; i++) {
                        handQuantities[i] = 0;
                    }

                    //reset and expressions state
                    for (int i=0; i<andExprIs.length; i++) {
                        andExprIs[i] = true;
                    }

                    int currentGroup = 0;

                    //for each draw
                    for (int draws=1; draws <= hand.length; draws++) {
                        //update hand state
                        int idx = indexes[dList.list[hand[draws-1]]];
                        if (idx != -1) {
                            handQuantities[idx]++;
                        }

                        //if current draws group checks at this many draws
                        if (drawsArray[currentGroup] == draws) {
                            //for each simple expression in the draw group
                            for(int simpleExpr=0; simpleExpr<drawsGroups[currentGroup].length; simpleExpr++) {
                                int[] pos = drawsGroups[currentGroup][simpleExpr];
                                //if the and expression has not been falsed yet
                                if (andExprIs[pos[0]]) {
                                    Restriction[][] expr = shortForms[pos[0]][pos[1]];
                                    boolean simpleExprIs = false;
                                    //for each and condition in the simple expression (~= or condition)
                                    for (int andCond=0; andCond<expr.length; andCond++) {
                                        Restriction[] cond = expr[andCond];
                                        boolean andCondIs = true;
                                        //compare hand states
                                        for(int r=0; r<handQuantities.length; r++) {
                                            //if relevant to the restriction
                                            if (cond[r].quantity != -1) {
                                                //if restriction false
                                                if (
                                                    handQuantities[r] < cond[r].quantity ||
                                                    cond[r].exact && cond[r].quantity < handQuantities[r]
                                                ) {
                                                    //stop checking the whole andCond
                                                    andCondIs = false;
                                                    break;
                                                }
                                            }
                                        }

                                        //if all restrictions true
                                        if (andCondIs) {
                                            //stop checking the simpleExpr
                                            simpleExprIs = true;
                                            break;
                                        }
                                        
                                    }

                                    if (!simpleExprIs) {
                                        //shortForms[pos[0]] has been falsed, ignore it
                                        andExprIs[pos[0]] = false;
                                    } else if (pos[1]+1 == shortForms[pos[0]].length) {
                                        //reached the end of shortForms[pos[0]] without falsing it
                                        //whole condition is satisfied
                                        return true;
                                    }
                                }
                            }

                            //move to next draws group
                            currentGroup++;
                        }
                    }
                    //no shortForms[n] has been proven true
                    //whole condition is not satisfied
                    return false;
                };
            };
        }
    }

    //use only after canonical form 
    public List<Integer> relevantIndexes() {
        if (this.condition != null) {
            return this.condition.relevantIndexes();
        } else if (this.expressions != null) {
            ArrayList<Integer> relevantIndexes = new ArrayList<>();

            for (Expression expr : this.expressions) {
                for (Integer idx : expr.relevantIndexes()) {
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

    public Expression(boolean constant) {
        this.constant = constant;
        this.draws = 0;
    }

    public Expression (Condition condition, int draws) {
        if (condition.constant != null) {
            this.constant = condition.constant;
        } else if (draws == 0) {
            this.constant = false;
        } else {
            this.condition = condition;
            this.draws = draws;
        }
    }

    public Expression (List<Expression> expressions, String operation) {
        this.expressions = expressions;
        this.operation = operation;
        this.draws = -1;
        for (Expression expr : this.expressions) {
            if (this.draws < expr.draws) {
                this.draws = expr.draws;
            }
        }
    }

    public Expression deepCopy() {
        if (this.condition != null) {
            return new Expression(this.condition.deepCopy(), this.draws);
        } else if (this.expressions != null) {
            ArrayList<Expression> expressions = new ArrayList<>();

            for (Expression expr : this.expressions) {
                expressions.add(expr.deepCopy());
            }

            return new Expression(expressions, this.operation);
        } else if (this.constant != null){
            return new Expression(this.constant);
        } else {
            return null;
        }
    }

    public Expression extractOr() {
        if (this.expressions != null) {
            if ("|".equals(this.operation)) {
                ArrayList<Expression> expressions = new ArrayList<>();
                for (Expression expr: this.expressions) {
                    expressions.add(expr.extractOr());
                }
                //precollapsing after or extraction to avoid nested ors
                return new Expression(expressions, "|").collapse();
            } else {
                //separate in or and notOr expressions
                ArrayList<Expression> orList = new ArrayList<>();
                ArrayList<Expression> notOrList = new ArrayList<>();
                for (Expression expr : this.expressions) {
                    Expression e = expr.extractOr();
                    if ("|".equals(e.operation)) {
                        orList.add(e);
                    } else {
                        notOrList.add(e);
                    }
                }

                //combine all or expressions
                ArrayList<Expression> opExpressions = new ArrayList<>();
                opExpressions.add(new Expression(new ArrayList<>(), this.operation));
                ArrayList<Expression> newOpExpressions;
                //for each or expression
                for (Expression orExpr : orList) {
                    newOpExpressions = new ArrayList<>();
                    //for each or subexpression
                    for (Expression orSubExpr : orExpr.expressions) {
                        //copy each operation expression and add a copy of the or subexpression
                        for (Expression opExpr : opExpressions) {
                            Expression newOpExpr = opExpr.deepCopy();
                            newOpExpr.expressions.add(orSubExpr.deepCopy());
                            newOpExpressions.add(newOpExpr);
                        }
                    }
                    opExpressions = newOpExpressions;
                }

                //add a copy of all not or expressions to each operation expression
                for (Expression opExpr : opExpressions) {
                    for (Expression notOrExpr : notOrList) {
                        opExpr.expressions.add(notOrExpr.deepCopy());
                    }
                }

                return new Expression(opExpressions, "|");
            }
        } else {
            return deepCopy();
        }
    }

    public Expression collapse() {
        return collapse(false);
    }


    public Expression collapse(boolean allowMerge) {
        if (this.expressions != null) {
            ArrayList<Expression> expressions = new ArrayList<>();

            for (Expression expr : this.expressions) {
                Expression e = expr.collapse(allowMerge);
                //collapse same operations
                if (this.operation.equals(e.operation)) {
                    for (Expression e2 : e.expressions) {
                        expressions.add(e2);
                    }
                } else if (expr.constant != null) {
                    if (this.operation.equals("&") && !expr.constant) {
                        //false term inside and operator, result is false
                        return new Expression(false);
                        //true term inside and operator is ignored
                    } else if (this.operation.equals("|") && expr.constant) {
                        //true term inside or operator, result is true
                        return new Expression(true);
                        //false term inside or operator is ignored
                    }

                } else {
                    expressions.add(e);
                }
            }

            if (allowMerge) {
                int total = expressions.size();
                for (int i=0; i<total; i++) {
                    if (expressions.get(i).condition != null ) {
                        ArrayList<Condition> mergeable = new ArrayList<>();
                        for (int j=0; j<total; j++) {
                            if (i != j && expressions.get(i).draws == expressions.get(j).draws
                                && expressions.get(j).condition != null
                            ) {
                                mergeable.add(expressions.get(j).condition);
                                expressions.remove(j);
                                total--;
                                if (j < i) {
                                    i--;
                                }
                                j--;
                            }
                        }
                        if (!mergeable.isEmpty()) {
                            mergeable.add(expressions.get(i).condition);
                            expressions.get(i).condition = new Condition(mergeable, this.operation).canonicalForm();
                        }
                    }
                }
            }

            if (expressions.size() == 0) {
                if (this.operation.equals("&")) {
                    return new Expression(true);
                } else if (this.operation.equals("|")) {
                    return new Expression(false);
                } else {
                    return null;
                }
            } else if (expressions.size() == 1) {
                return expressions.get(0);
            } else {
                expressions.sort((expr1, expr2) -> expr1.draws - expr2.draws);
                return new Expression(expressions, this.operation);
            }
        } else if (this.condition != null) {
            return new Expression(this.condition.restrictMaximum(this.draws), this.draws);
        } else {
            return deepCopy();
        }
    }

    public Expression purge() {
        if (this.expressions != null) {

            List<Expression> expressions = new ArrayList<>();

            for (Expression expr1 : this.expressions) {
                expressions.add(expr1.purge());
            }

            int total = expressions.size();

            for (int i=0; i<total; i++) {
                for (int j=0; j<total; j++) {
                    if (i != j) {
                        if (
                            "|".equals(this.operation) && expressions.get(j).implies(expressions.get(i)) ||
                            "&".equals(this.operation) && expressions.get(i).implies(expressions.get(j))
                        ) {
                            expressions.remove(j);
                            total--;
                            if(j < i) {
                                i--;
                            }
                            j--;
                        }
                    }
                }
            }

            List<Expression> newExpressions;
            //Multidependancy & Compatibility
            if ("&".equals(this.operation)) {
                newExpressions = new ArrayList<>();

                for (Expression expr1 : expressions) {
                    Condition mergedCond = new Condition(new ArrayList<>(), "&");
                    for (Expression expr2 : expressions) {
                        if (expr1 != expr2 && expr2.draws <= expr1.draws) {
                            mergedCond.conditions.add(expr2.condition);
                        }
                    }

                    mergedCond = mergedCond.canonicalForm();
                    //Multidependancy
                    if (!mergedCond.implies(expr1.condition)) {
                        newExpressions.add(expr1);
                    }

                    mergedCond = new Condition(Arrays.asList(mergedCond, expr1.condition), "&");
                    mergedCond = mergedCond.canonicalForm();
                    //Compatibility
                    if (Boolean.FALSE.equals(mergedCond.restrictMaximum(expr1.draws).constant)) {
                        return new Expression(false);
                    }
                }

                expressions = newExpressions;
            }

            return new Expression(expressions, this.operation);
        } else {
            return deepCopy();
        }
    }

    public Expression condition2Expression() {
        if (this.constant != null) {
            return new Expression(this.constant);
        } else if (this.expressions != null) {
            ArrayList<Expression> expressions = new ArrayList<>();
            for (Expression e : this.expressions) {
                expressions.add(e.condition2Expression());
            }
            return new Expression(expressions, this.operation);
        } else if (this.condition != null) {
            return this.condition.condition2Expression(this.draws);
        } else {
            return null;
        }
    }

    public boolean same(Expression that) {
        return this.implies(that) && that.implies(this);
    }

    public class Factor {
        public Expression expression;
        public int count;

        public Factor(Expression expression) {
            this.expression = expression;
            this.count = 1;
        }
    }

    public Expression factorize() {
        if ("|".equals(this.operation)) {
            //Find best factors
            ArrayList<Factor> factors = new ArrayList<>();

            for (Expression expr1 : this.expressions) {
                for (Expression expr2 : (expr1.expressions != null ? expr1.expressions : Arrays.asList(expr1))) {
                    boolean found = false;
                    for (Factor factor : factors) {
                        if (expr2.same(factor.expression)) {
                            factor.count++;
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        factors.add(new Factor(expr2.deepCopy()));
                    }
                }
            }

            factors.sort((f1, f2) -> f2.count - f1.count);

            if (factors.get(0).count == 1) {
                return this.deepCopy();
            } else {
                //if best factor repeats, separe and refactor each subgroup
                Expression factor = factors.get(0).expression;
                ArrayList<Expression> factorized = new ArrayList<>();
                ArrayList<Expression> notFactorized = new ArrayList<>();

                for(Expression expr1 : this.expressions) {
                    boolean found = false;
                    for (Expression expr2 : (expr1.expressions != null ? expr1.expressions : Arrays.asList(expr1))) {
                        if (factor.same(expr2)) {
                            found = true;
                            break;
                        }
                    }

                    if (found) {
                        ArrayList<Expression> remainder = new ArrayList<>();
                        for (Expression expr2 : (expr1.expressions != null ? expr1.expressions : Arrays.asList(expr1))) {
                            if (!factor.same(expr2)) {
                                remainder.add(expr2.deepCopy());
                            }
                        }

                        if (remainder.size() == 0) {
                            factorized.add(new Expression(true));
                        } else if (remainder.size() == 1) {
                            factorized.add(remainder.get(0));
                        } else {
                            factorized.add(new Expression(remainder, "&"));
                        }
                    } else {
                        notFactorized.add(expr1);
                    }
                }

                Expression a1, a2;
                
                a1 = new Expression(factorized, "|").factorize();
                

                if (notFactorized.isEmpty()) {
                    a2 = new Expression(false);
                } else {
                    a2 = new Expression(notFactorized, "|").factorize();
                }

                return new Expression(Arrays.asList(
                            new Expression(Arrays.asList(
                                factor,
                                a1
                            ), "&"),
                            a2
                    ), "|").collapse(true);
            }
        } else {
            return deepCopy();
        }
    }

    public Expression canonicalForm() {
        Expression current = canonicalConditions();
        current = current.condition2Expression(); //Fernando Deinller
        current = current.collapse();
        current = current.extractOr();
        current = current.collapse();
        current = current.purge();
        current = current.collapse(true);
        current = current.factorize();

        return current;
    }

    //apply only after canonicalForm
    public Expression optimize(ParserContext parserContext) {
        if (this.expressions != null) {
            ArrayList<Expression> expressions = new ArrayList<>();
            for (Expression expr : this.expressions) {
                expressions.add(expr.optimize(parserContext));
            }
            return new Expression(expressions, this.operation);
        } else if (this.condition != null) {
            Condition condition = this.condition.optimize(parserContext).canonicalForm();
            if (!Boolean.FALSE.equals(condition.constant) && parserContext.decklist.list.length <= this.draws) {
                return new Expression(true);
            } else {
                return new Expression(condition, this.draws);
            }
        } else {
            return deepCopy();
        }
    }

    public boolean implies(Expression that) {
        if (this.constant != null && that.constant != null) {
            return this.constant.equals(that.constant);
        } else if (this.constant != null || that.constant != null) {
            return false;
        } else if (this.condition != null && that.condition != null) {
            return this.draws <= that.draws && this.condition.implies(that.condition);
        } else if ("&".equals(this.operation) && that.condition != null) {
            //if any element of this (&) implies that (simple), this implies that
            for (Expression thisE : this.expressions) {
                if (thisE.implies(that)) {
                    return true;
                }
            }
            return false;
        } else if (!"|".equals(this.operation) && "&".equals(that.operation)) {
            //if all elements of that (&) are implied by this (&/simple), this implies that
            for (Expression thatE : that.expressions) {
                if(!this.implies(thatE)) {
                    return false;
                }                
            }
            return true;
        } else if (!"|".equals(this.operation) && "|".equals(that.operation)) {
            //if any element of that (|) is implied by this (&/simple), this implies that
            for (Expression thatE : that.expressions) {
                if (this.implies(thatE)) {
                    return true;
                }
            }
            return false;
        } else if ("|".equals(this.operation)) {
            //if all elements of this (|) imply that (|/&/simple), this implies that
            for (Expression thisE : this.expressions) {
                if (!thisE.implies(that)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public Expression canonicalConditions() {
        if (this.condition != null ) {
            Condition cond = this.condition.canonicalFormNode();
            if (cond.constant != null) {
                return new Expression(cond.constant);
            } else {
                return new Expression(cond, draws);
            }
        } else if (this.expressions != null) {
            ArrayList<Expression> expressions = new ArrayList<>();
            for (Expression expr : this.expressions) {
                expressions.add(expr.canonicalForm());
            }
            return new Expression(expressions, operation);
        } else {
            return deepCopy();
        }
    }

    @Override
    public String toString() {
        String out = "";
        if (condition != null) {
            out += "@" + Integer.toString(draws) + "\n\t" + condition.toString().replaceAll("\n", "\n\t");
        }
        if (expressions != null) {
            out += "Expression "+operation;
            for (Expression exp : expressions) {
                out += "\n\t" + exp.toString().replaceAll("\n", "\n\t");
            }
        }
        if (constant != null) {
            out += constant.toString();
        }

        return out;
    }
}