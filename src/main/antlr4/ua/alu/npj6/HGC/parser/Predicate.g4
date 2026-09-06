grammar Predicate;

handFile: (line)+ EOF ;

line: NAME EQ expression EOL            # HandLine
    | NAME EQ role EOL                  # RoleLine
    ;

expression: condition AT NUMBER         # SimpleExpr
          | expression AND expression   # AndExpr
          | expression OR expression    # OrExpr
          | LP expression RP            # PExpr
          ;

condition: restriction                  # SimpleCond
         | condition AND condition      # AndCond
         | condition XAND condition     # XAndCond
         | condition OR condition       # OrCond
         | LP condition RP              # PCond
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

COMMENT: '//' ~[\n]* '\n' -> skip;

WS : [ \t\r\n]+ -> skip ;
