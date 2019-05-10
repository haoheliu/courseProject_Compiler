CourseProject - Compiler

<u>Using java to implement a simple compiler</u>

<!-- TOC -->
autoauto- [1. Overall mindmap](#1-overall-mindmap)auto- [2. Syntax support](#2-syntax-support)auto    - [2.1. Arithmetic](#21-arithmetic)auto        - [2.1.1. calculations - +,-,*,/](#211-calculations----)auto        - [2.1.2. boolean expressions](#212-boolean-expressions)auto        - [2.1.3. comparision - >,<,<=,>=,!=,==](#213-comparision---)auto        - [2.1.4. assignment statement](#214-assignment-statement)auto    - [2.2. Selection and loop](#22-selection-and-loop)auto        - [2.2.1. if-else](#221-if-else)auto        - [2.2.2. switch](#222-switch)auto        - [2.2.3. while](#223-while)auto    - [2.3. Function](#23-function)auto        - [2.3.1. function defination](#231-function-defination)auto        - [2.3.2. function call](#232-function-call)auto    - [2.4. Built-in functions](#24-built-in-functions)auto        - [2.4.1. println(&lt;STRING&gt;/variable/constant)](#241-printlnltstringgtvariableconstant)auto        - [2.4.2. readint()](#242-readint)auto    - [2.5. Comment](#25-comment)auto    - [2.6. Error report](#26-error-report)auto- [3. Design detail](#3-design-detail)auto    - [3.1. Arithmetic](#31-arithmetic)auto        - [3.1.1. calculations - +,-,*,/](#311-calculations----)auto        - [3.1.2. boolean expressions](#312-boolean-expressions)auto        - [3.1.3. comparision - >,<,<=,>=,!=,==](#313-comparision---)auto        - [3.1.4. assignment statement](#314-assignment-statement)auto    - [3.2. Selection and loop](#32-selection-and-loop)auto        - [3.2.1. if-else](#321-if-else)auto        - [3.2.2. switch](#322-switch)auto        - [3.2.3. while](#323-while)auto    - [3.3. Function](#33-function)auto        - [3.3.1. function defination](#331-function-defination)auto        - [3.3.2. function call](#332-function-call)auto    - [3.4. Built-in functions](#34-built-in-functions)auto        - [3.4.1. println(&lt;STRING&gt;/variable/constant)](#341-printlnltstringgtvariableconstant)auto        - [3.4.2. readint()](#342-readint)auto    - [3.5. Comment](#35-comment)auto    - [3.6. Error report](#36-error-report)auto- [4. Grammer Designed](#4-grammer-designed)auto- [5. Problem encountered](#5-problem-encountered)auto    - [5.1. Problem with function call stack](#51-problem-with-function-call-stack)auto    - [5.2. Problem with register management](#52-problem-with-register-management)autoauto
<!-- /TOC -->

# 1. Overall mindmap
Lexical analysis-&gt;Grammer parser-&gt;Recursive descent->Generate MIPS code

---

# 2. Syntax support

## 2.1. Arithmetic
### 2.1.1. calculations - +,-,*,/
### 2.1.2. boolean expressions 
### 2.1.3. comparision - >,<,<=,>=,!=,==
### 2.1.4. assignment statement

## 2.2. Selection and loop
### 2.2.1. if-else
### 2.2.2. switch
### 2.2.3. while

## 2.3. Function
### 2.3.1. function defination
- variable declaration
- return statement
### 2.3.2. function call
- recursive function call

## 2.4. Built-in functions
### 2.4.1. println(&lt;STRING&gt;/variable/constant)
### 2.4.2. readint()

## 2.5. Comment

## 2.6. Error report

---

# 3. Design detail

## 3.1. Arithmetic
### 3.1.1. calculations - +,-,*,/
### 3.1.2. boolean expressions 
### 3.1.3. comparision - >,<,<=,>=,!=,==
### 3.1.4. assignment statement

## 3.2. Selection and loop
### 3.2.1. if-else
### 3.2.2. switch
### 3.2.3. while

## 3.3. Function
### 3.3.1. function defination
- variable declaration
- return statement

### 3.3.2. function call
- function call stack

## 3.4. Built-in functions
### 3.4.1. println(&lt;STRING&gt;/variable/constant)
### 3.4.2. readint()

## 3.5. Comment

## 3.6. Error report

---

# 4. Grammer Designed

**program** 					-&gt;**programUnitList**  &lt;EOF&gt;  
|&lt;EOF&gt;

**programUnitList**  			-&gt;**programUnit**  **programUnitList**  {“def”}  
|ε

**programUnit**  				-&gt;**functionDefinition** {“def”}  
|ε

**functionDefinition**-&gt;”def” ”void” &lt;ID&gt; “(” **parameterList** “)”
“{” **localDeclarations** **statementList** “}”

**parameterList**				-&gt;	**parameter** **parameterTail**

**parameter**					-&gt;	”int” &lt;ID&gt;

**parameterTail** 				-&gt;	”,” **parameter** **parameterTail**  
|ε{“)”}

**localDeclarations**			-&gt;	”int” &lt;ID&gt; **localTail** “;”  
|ε{&lt;ID&gt;,”println”,”{”,”while”,”if”,”return”,”cal”}

**localTail** 					-&gt;	”,” &lt;ID&gt; **localTail**  
|ε {”;”}

---

**statementList** 				-&gt; 	**statement**  **statementList**  
|ε{&lt;EOF&gt;,”}”,”return”}

**statement** 					-&gt; 	**assignmentAndBoolen**{&lt;ID&gt;}

**statement**					-&gt;	**printlnStatement**{”println”}

**statement** 					-&gt;	**compoundStatement**{“{”}

**statement**					-&gt;	**whileStatement**{“while”}

**statement**					-&gt;	**ifStatement**{“if”}

**statement**					-&gt;	**switchStatement** {“switch”}

**statement** 					-&gt;	**returnStatement**{“return”}

**statement** 					-&gt; 	**functionCall** {“cal ”}

---

**assignmentAndBoolen**		-&gt;	&lt;ID&gt;  **assignmentStatement**

**assignmentStatement**		-&gt;	”=”  **expr**  ”;”

**argumentList**				-&gt;	**expr**  **argtail**  
|ε{“)”} {&lt;UNSIGNED&gt;,”+”,”-”,&lt;ID&gt;,”(”,&lt;STRING&gt;}

---


**argtail**						-&gt;	“,” **expr** **argtail**  
|ε {“)”}

**compoundStatement**		-&gt;	”{” **statementList** “}”

**printlnStatement**			-&gt;	”println”  ”(”  **expr** ”)”  “;” {”println”}

**whileStatement**				-&gt;	”while”  “(”  **expr**  “)”  **statement**

**ifStatement** 			    	-&gt;	”if”  “(”  **expr**  “)”  **statement**  **elsePart**

**elseStatement**(代码上实现) 	-&gt;	”else” **statement**

**switchStatement** 			-&gt;”switch”  “(” **expr** “)” “{”
**caseStatementList** **defaultStatement** “}”

**caseStatementList**			-&gt;**caseStatement** **caseStatementList**{”case”}  
|ε

**caseStatement**				-&gt;case numbers”:” **StatementList**

**defaultStatement**			-&gt;”default” “:” **StatementList**  
|ε

**numbers**					-&gt;	&lt;UNSIGNED&gt;

**compoundStatement**		-&gt;	”{” **statementList** “}”

**returnStatement**			-&gt;	”return“ **expr** “;”  
|ε {&lt;UNSIGNED&gt;,”+”,”-”,&lt;ID&gt;,”(”,&lt;STRING&gt;}

---

**expr**							-&gt;	term termList
{&lt;UNSIGNED&gt;,”+”,”-”,&lt;ID&gt;,”(”,&lt;STRING&gt;}

**termList**						-&gt;	”+”  term termList  
|”-” **term** **termlist**  
|”==”  **expr**  
|”&gt;=”  **expr**  
|”&lt;=”  **expr**  
|”&gt;”  **expr**  
|”&lt;”  **expr**  
|**boolenExpression**	{“and”,”or”,“)”,”;”,”,”}  
|ε{“)”,”;”,”,”}


**functionCall**					-&gt;	“cal” &lt;ID&gt; “(” argumentList “)” “;”


**boolenExpression**			-&gt;	“and” **expr** **boolenExpression**  
|“or” **expr** **boolenExpression**  
|ε{“)”,”;”,”,”}


**term**						-&gt;	**factor**  **factorList**{&lt;UNSIGNED&gt;,”+”,”-”,&lt;ID&gt;,”(”,&lt;STRING&gt;}  
|**functionCall**{“cal ”}


**factor**						-&gt;	&lt;UNSIGNED&gt;     {&lt;UNSIGNED&gt;}  
| ”+”  &lt;UNSIGNED&gt; {“+”}  
| ”-”	  &lt;UNSIGNED&gt; {“-”}  
| &lt;ID&gt; {&lt;ID&gt;}  
| ”(”  **expr**  “)” {“(”}  
|**functionCall**  
|&lt;STRING&gt;

**factorList**					-&gt;	”*” **factor** **factorList**   {“*”}  
|”/” **factor** **factorList**	{“/”}  
|ε {“)”,”;”,”+”}

# 5. Problem encountered
## 5.1. Problem with function call stack
## 5.2. Problem with register management