# HyperGeometric Calculator
Game agnostic hand probability calculator. Monte Carlo method. Define a set of hands and estimate their probability. WIP.

Still half-baked, output includes test prints and some hand definitions could take unreasonable amounts of compute time.

`mvn clean verify` and `hgc -dF DECKFILE -dH HANDFILE` to estimate the probabilities of all hands in HANDFILE based on the DECKFILE list. Check out the examples folder. You may use `--hands HANDNAME1 HANDNAME2 ...` to calculate only some hands. HANDNAMEs should not include double quotes.

## Hand File
All names in the hand file need to be enclosed in double quotes and are case sensitive. Spaces, tabs and new lines are ignored (unless in between double quotes). Use `//` to start line comments. Every statement in the hand file should end with a semicolon. Every statement in the hand file must be either a named role, a named condition or a named expression.

### Roles
In some situations multiple cards can accomplish the same goal. To represent such a situation you may write `( "name of card 1" | "name of card 2" | ... )`. You can use these directly in hand definitions or name them if you want to use them multiple times. To name a role write `"role name" = ( "card1" | "card2" ); ` in the file. To use a named role in hand definitions the role must be defined before the hand that uses it.

### Restrictions
To ask for N or more copies of a card write `"card"xN`, to ask for exactly N copies of a card write `"card"!xN`. Both operators work with roles too. A role or card name without `x` or `!x` is assumed to mean `"card"x1` to increase readability.

### Conditions
You can combine restrictions in your hand definitions using the operators `|`, `&` and `^&`. Such a combination is called a condition. You may also combine conditions with these operators.
 - `|` is the or operator: `"card1"x1 | "card2"x2` would accept any hand containing 1 card1, 2 card2 or both of them.
 - `&` is the and operator: `"card1"x1 & "card2"x2` will only accept a hand with 1 card1 and 2 card2.
 - `^&` is the exclusive and operator, it works almost identically to `&` but forces each side to be satisfied by different sets of cards in the hand: `"card1" & "card1"` would be satisfied by a hand with 1 card1, but `"card1" ^& "card1"` asks for a hand with 2 card1.

Having two different and operators makes it easier to define hands with many roles in them. The operator precedence is `&` > `^&` > `|`. This means if you wrote `"card1" | "card2" & "card3" ^& "card1"` it would be read as `"card1" | (("card2" & "card3") ^& "card1")` instead of `("card1" | "card2") ^& ("card3" & "card1)"`. You can use parenthesis when writing conditions.

### Expressions (hands)

Last but not least, you need to define the number of draws in which you want the conditions to be satisfied. Adding `@ N` at the end of a condition means you want it to happen after drawing N cards: `"card1"x!3 @ 7` would estimate the probability of drawing exactly 3 card1 after 7 draws from the starting deck.

You can use the `|` and `&` operators to combine different expressions: `"card1" @ 7 & "card2" @ 8` would mean drawing at least 1 card1 in 7 draws, then drawing the 8th card and having at least 1 card2 after that. To add an expression to the hand file you can use the same syntax as with named roles `"hand name" = "card1" @ 7;`. You can use named expressions inside other expressions, but you must specify that it is not a role name by writing `HAND "hand name"`.

Feel free to suggest any features you'd find useful in the hand defining language.
