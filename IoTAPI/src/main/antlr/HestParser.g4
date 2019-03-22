parser grammar HestParser;

options { tokenVocab=HestLexer; }
content: (dataset | rulee)* ;

rulee: 'rule' '{' (config|variable|device|output|run)* '}';
config: 'config' time;
time: 'every' timeDefinition | 'once' timeDefinition | interval;
timeDefinition: INTLIT unit='day' | INTLIT unit='hour' | INTLIT unit='min';
interval: 'from' fromDate = DATELIT fromTime = TIMELIT 'to' toDate=DATELIT toTime=TIMELIT 'every' timeDefinition; //TODO fra 'every' er tilføjet men ikke håndteret
run: 'run' '{' condition=expression? output* varpath* '}';


/*
condition: cName=STRINGLIT ((eqOperator right = condition)?);  //And or's senere
exp: cName=condition ((eqOperator right = condition)?);*/



eqOperator: '<' | '==' | '!=' | '<=' | '>' | '=>';
output:  device path  ('post' | 'get') parameter*;
device: 'device' deviceName=ID;
path: 'path' ID;
parameter: parName=STRINGLIT parValue=varTypes;
varpath: ('var' varName=ID '=')? output;
dataset: 'dataset' '{' tag  pkey  name  format  variable*  '}';
tag: 'tag' '=' tagName=STRINGLIT;
pkey: 'key' '=' pKey=INTLIT ;
name: 'name' '=' nameName=STRINGLIT;
format: 'format' '=' formatName=('JSON' | 'XML');
variable: 'var' ID '=' expression;
varTypes: STRINGLIT|DECLIT|BOOL;
string: QOUTE ID QOUTE;

binaryOp: '&&' | '||';

expression:
/*   LPAREN expression RPAREN                       #parenExpression
 | NOT expression                                 #notExpression
 |*/ left=expression op=binaryOp right=expression #comparatorExpression
 | left=expression op=eqOperator  right=expression   #binaryExpression
 | BOOL                                           #boolExpression
 | ID                                             #identifierExpression
 | DECLIT                                         #decimalExpression
 | STRINGLIT                                      #stringExpression
 ;