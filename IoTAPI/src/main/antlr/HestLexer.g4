lexer grammar HestLexer;

// Whitespace
WS:                 [ \t\r\n\u000C]+ -> skip;

// Keywords
VAR                : 'var' ;

// Literals
INTLIT             : [1-9][0-9]*;
DECLIT             : '0'|[1-9][0-9]* ('.' [0-9]+)?;
TIMELIT            : [0-2][0-9]':'[0-5][0-9];
DATELIT            : [0-9][0-9][0-9][0-9]':'[0-1][0-9]':'[0-3][0-9];
STRINGLIT          : '"' ~["\\\r\n]* '"';
BOOL               : 'false' | 'true';
NOT                : '!';
QOUTE              : '"';

// Operators
PLUS               : '+' ;
MINUS              : '-' ;
ASTERISK           : '*' ;
DIVISION           : '/' ;
ASSIGN             : '=' ;
LPAREN             : '(' ;
RPAREN             : ')' ;
LESSTHAN: '<';
NOTEQUALS: '!=';


EQUALS: '==';
EQUALSLESS: '<=';
MORETHAN: '>';
EQUALSMORE: '=>';
LBRACKET : '{';
RBRACKET : '}';

DATASET: 'dataset' ;
TAG : 'tag' ;
HEADER : 'header' ;
NAME : 'name' ;
FORMAT : 'format' ;
CONFIG : 'config';
POST: 'post';
GET: 'get';
EVERY: 'every';
ONCE: 'once';
DAY: 'day';
HOUR: 'hour';
SECONDS: 'seconds';
MIN: 'min';
FROM: 'from';
TO: 'to';
RUN: 'run';
DEVICE: 'device';
PATH: 'path';
RULE :'rule';
// Formats
JSON : 'JSON';
XML : 'XML' ;
KEY : 'key';
OR: '||';
AND: '&&';
// Identifiers
ID: [a-z][A-Za-z0-9_]* ;