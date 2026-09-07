grammar Predicate;

handFile: (line)+ EOF ;

line: NAME EQ expression EOL            # HandLine
    | NAME EQ role EOL                  # RoleLine
    | COND_T NAME EQ condition EOL      # ConditionLine
    ;

expression: condition AT NUMBER         # SimpleExpr
          | expression AND expression   # AndExpr
          | expression OR expression    # OrExpr
          | LP expression RP            # PExpr
          | EXPR_T NAME                 # NamedExpr
          ;

condition: restriction                  # SimpleCond
         | condition AND condition      # AndCond
         | condition XAND condition     # XAndCond
         | condition OR condition       # OrCond
         | LP condition RP              # PCond
         | COND_T NAME                  # NamedCond
         ;

restriction: role                       # SimpleRestrict
         | role TIMES NUMBER            # TimesRestrict
         | role EXACT_TIMES NUMBER      # ExactRestrict
         ;

role: NAME                              # SimpleRole
    | LP NAME (OR NAME)+ RP             # MultiRole
    ;

LP : '(';

RP : ')';

AT : '@';

TIMES : 'x';
EXACT_TIMES: '!x';

AND : '&';
XAND: '^&';
OR: '|';

NAME : '"' ~[\t\r\n"]+ '"' ;

NUMBER : [0-9]+ ;

EOL: ';';

EQ: '=';

COND_T : 'COND';

EXPR_T : 'HAND';

COMMENT: '//' ~[\n]* '\n' -> skip;

WS : [ \t\r\n]+ -> skip ;
