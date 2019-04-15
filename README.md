# courseProject_Compiler

Using java to implement a simple compiler

词法分析->语法分析->生成四元式->生成基于MIPS指令集的汇编代码


program 					-> statementList <EOF> | <EOF>
{<ID>,”println”,<EOF>}


statementList 				-> statement  statementList {<ID>,”println”}
|ε{<EOF>}


statement 					-> assignmentStatement {<ID>}
statement					->printlnStatement{”println”}


assignmentStatement 		-><ID>  ”=”  expr  ”;”  {<ID>}
printlnStatement			->”println”  ”(”  expr”)”  “;” {”println”}


expr						->term termList 
{<UNSIGNED>,’+’,’-’,<ID>,’(’}

termList					->”+”  term termList  {‘+’}
|ε{‘)’,’;’}
term						->factor  factorList 
{<UNSIGNED>,’+’,’-’,<ID>,’(’}

factor						-><UNSIGNED>     {<UNSIGNED>}
| ”+”  <UNSIGNED> {‘+’}
| ”-”	  <UNSIGNED> {‘-’}
| <ID> {<ID>}
| ”(”  expr  “)” {‘(’}

factorList					->”*” factor factorList   {‘*’}
|ε {‘)’,’;’,’+’}
