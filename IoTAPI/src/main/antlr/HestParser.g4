parser grammar HestParser;

options { tokenVocab=HestLexer; }
content: (dataset | rulee)* ;

rulee: 'rule' '{' (config|variable|condition|device|output|run)* '}';
config: 'config' time;
time: 'every' timeDefinition | 'once' timeDefinition | interval; //Nok mere...
timeDefinition: INTLIT 'day' | INTLIT 'hour' | INTLIT 'min';
interval: 'from' TIMELIT 'to' TIMELIT;
run: 'run' '{' (condition (output|varpath)+ | output|varpath+)+ '}';
condition: cName=ID ((eqOperator condition)?);  //And or's senere
eqOperator: '<' | '==' | '<=' | '>' | '=>';
output:  device path  ('post' | 'get') parameter*;
device: 'device' ID;
path: 'path' ID;
parameter: STRINGLIT varTypes;
varpath: 'var' ID '=' output;
dataset: 'dataset' '{' (tag | header | name | format | variable)* '}';
tag: 'tag' '=' STRINGLIT;
header: 'header' '=' STRINGLIT;
name: 'name' '=' STRINGLIT;
format: 'format' '=' ('JSON' | 'XML');
variable: VAR ID '=' varTypes;
varTypes: INTLIT|STRINGLIT|DECLIT|BOOL;
