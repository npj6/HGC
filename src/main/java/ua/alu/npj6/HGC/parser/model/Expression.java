package ua.alu.npj6.HGC.parser.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class Expression {
    Condition condition = null;

    List<Expression> expressions = null;
    String operation = null;

    int draws;

    Boolean constant = null;

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

                } else if (allowMerge) {
                    boolean merged = false;

                    for (Expression e2 : expressions) {
                        if (e2.draws == e.draws && e2.condition != null && e.condition != null) {
                            e2.condition = new Condition(Arrays.asList(e2.condition, e.condition), this.operation).canonicalForm();
                            merged = true;
                        }
                    }

                    if (!merged) {
                        expressions.add(e);
                    }
                } else {
                    expressions.add(e);
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

            List<Expression> newExpressions;
            boolean changed = true;

            while (changed) {
                changed = false;

                for (Expression expr1 : expressions) {
                    newExpressions = new ArrayList<>();
                    newExpressions.add(expr1.deepCopy());

                    for (Expression expr2 : expressions) {
                        if (expr1 != expr2) {
                            if (
                                "|".equals(this.operation) && expr2.implies(expr1)
                                || "&".equals(this.operation) && expr1.implies(expr2)
                            ) {
                                changed = true;
                            } else {
                                newExpressions.add(expr2.deepCopy());
                            }
                        }
                    }

                    if (changed) {
                        expressions = newExpressions;
                        break;
                    }
                }
            }

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
            Condition cond = this.condition.canonicalForm();
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