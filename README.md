CourseProject - Compiler

<u>Using java to implement a simple compiler</u>

<!-- TOC -->autoauto- [1. Overall mindmap](#1-overall-mindmap)auto- [2. Syntax support](#2-syntax-support)auto    - [2.1. Arithmetic](#21-arithmetic)auto        - [2.1.1. calculations - +,-,*,/](#211-calculations----)auto        - [2.1.2. boolean expressions](#212-boolean-expressions)auto        - [2.1.3. comparision - >,<,<=,>=,!=,==](#213-comparision---)auto        - [2.1.4. assignment statement](#214-assignment-statement)auto    - [2.2. Selection and loop](#22-selection-and-loop)auto        - [2.2.1. if-else](#221-if-else)auto        - [2.2.2. switch](#222-switch)auto        - [2.2.3. while](#223-while)auto    - [2.3. Function](#23-function)auto        - [2.3.1. function defination](#231-function-defination)auto        - [2.3.2. function call](#232-function-call)auto    - [2.4. Built-in functions](#24-built-in-functions)auto        - [2.4.1. println(&lt;STRING&gt;/variable/constant)](#241-printlnltstringgtvariableconstant)auto        - [2.4.2. readint()](#242-readint)auto    - [2.5. Comment](#25-comment)auto    - [2.6. Error report](#26-error-report)auto- [3. Design detail](#3-design-detail)auto    - [3.1. Arithmetic](#31-arithmetic)auto        - [3.1.1. calculations - +,-,*,/](#311-calculations----)auto        - [3.1.2. boolean expressions](#312-boolean-expressions)auto        - [3.1.3. comparision - >,<,<=,>=,!=,==](#313-comparision---)auto        - [3.1.4. assignment statement](#314-assignment-statement)auto    - [3.2. Selection and loop](#32-selection-and-loop)auto        - [3.2.1. if-else](#321-if-else)auto        - [3.2.2. switch](#322-switch)auto        - [3.2.3. while](#323-while)auto    - [3.3. Function](#33-function)auto        - [3.3.1. function defination](#331-function-defination)auto        - [3.3.2. function call](#332-function-call)auto    - [3.4. Built-in functions](#34-built-in-functions)auto        - [3.4.1. println(&lt;STRING&gt;/variable/constant)](#341-printlnltstringgtvariableconstant)auto        - [3.4.2. readint()](#342-readint)auto    - [3.5. Comment](#35-comment)auto    - [3.6. Error report](#36-error-report)auto- [4. Grammer Designed](#4-grammer-designed)auto- [5. Problem encountered](#5-problem-encountered)auto    - [5.1. Problem with function call stack](#51-problem-with-function-call-stack)auto    - [5.2. Problem with register management](#52-problem-with-register-management)autoauto<!-- /TOC -->

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
- break
- continue

### 2.2.4. goto & dest
jump instruction and specify the place of destination
```c
def void main()
{
    int a,b;
    a = 10;
    b = 6;
    while(a >= 3)
    {
        println(b);
        println("\n");
        if(a == 5)
        {
            println("Use goto to jump to the label\n");
            goto First;
        }
        a = a-1;
    }
    dest First;
    println("Finish!");
    return 0;
}
```


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
## 2.7. Global redefined error
- Error: global1 has already been defined!
```c
int global1,global1;

def void main()
{
    int n,times;
    times = 6;
    return 0;
}
```
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

**programUnit**  				->**functionDefinition** {“def”}  
|**globalDeclarations** {"int"}  
|ε

**globalDeclarations**			-&gt;	”int” &lt;ID&gt; **globalTail** “;”  
|ε{&lt;ID&gt;,”println”,”{”,”while”,”if”,”return”,”cal”}

**globalTail** 					-&gt;	”,” &lt;ID&gt; **globalTail**  
|ε {”;”}

**functionDefinition**-&gt;”def” ”void” &lt;ID&gt; “(” **parameterList** “)”
“{” **localDeclarations** **statementList** “}”

**parameterList**				-&gt;	**parameter** **parameterTail**

**parameter**					-&gt;	”int” &lt;ID&gt;

**parameterTail** 				-&gt;	”,” **parameter** **parameterTail**  
|ε{“)”}


**localDeclarations**			->  
|"array" "[" &lt;UNSIGNED&gt; "]" ";"  **localDeclarations**   
|”int” &lt;ID&gt; **localTail** “;” **localDeclarations** 
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

**statement**              -> **jumpStatement** {"break","goto","continue","dest"}

**statement**              -> **arrayStatement** {"array"}

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

**jumpStatement**->"goto"  
|"break"  
|"continue"  
|"dest"

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

<table>
   <tr>
      <td>
         Function call stack
      </td>
      <td>
         comment
      </td>
   </tr>
   <tr>
      <td>
         $a0
      </td>
      <td>
         "store the
      </td>
   </tr>
   <tr>
      <td>
         arguments "
      </td>
   </tr>
   <tr>
      <td>
         ...
      </td>
      <td>
         
      </td>
   </tr>
   <tr>
      <td>
         $a1
      </td>
      <td>
         
      </td>
   </tr>
   <tr>
      <td>
         $ra
      </td>
      <td>
         return address
      </td>
   </tr>
   <tr>
      <td>
         $fp
      </td>
      <td>
         old value of $fp
      </td>
   </tr>
   <tr>
      <td>
         $localVal1
      </td>
      <td>
         "local declaration 
      </td>
   </tr>
   <tr>
      <td>
         in a function"function
      </td>
   </tr>
   <tr>
      <td>
         $localVal2
      </td>
      <td>
         
      </td>
   </tr>
   <tr>
      <td>
         ...
      </td>
      <td>
         
      </td>
   </tr>
   <tr>
      <td>
         $localValn
      </td>
      <td>
         
      </td>
   </tr>
   <tr>
      <td>
         $regsave_s1
      </td>
      <td>
         "register s 
      </td>
   </tr>
   <tr>
      <td>
         needed to save"
      </td>
   </tr>
   <tr>
      <td>
         $regsave_s2
      </td>
      <td>
         
      </td>
   </tr>
   <tr>
      <td>
         ...
      </td>
      <td>
         
      </td>
   </tr>
   <tr>
      <td>
         $regsave_sn
      </td>
      <td>
         
      </td>
   </tr>
   <tr>
      <td>
         $regsave_t1
      </td>
      <td>
         "register t 
      </td>
   </tr>
   <tr>
      <td>
         needed to save"
      </td>
   </tr>
   <tr>
      <td>
         $regsave_t2
      </td>
      <td>
         
      </td>
   </tr>
   <tr>
      <td>
         ...
      </td>
      <td>
         
      </td>
   </tr>
   <tr>
      <td>
         $regsave_tn
      </td>
      <td>
         
      </td>
   </tr>
   <tr>
      <td>
         
      </td>
   </tr>
</table>

## 5.2. Problem with register management

# 6. Test cases:
## 6.1. Recursive function calling & breakStatement &continue Statement & globalStatement
```c

int global1,global2;

def void factor( int N ){
    global1 = global1 + 1;
    println("\nThe global value: ");
    println(global1);
	if( N > 1 ){
		return N * cal factor( N - 1 );
	}
	else{
		return 1;
	}
}

def void main()
{
    int n,times;
    global1 = 1;
    times = 6;
    while(times >= 0)
    {
        n = cal factor(times);
        println("\nResult of factor:");
        println(times);
        println(" - ");
        println(n);
        println(" - ");
        if(times == 4)
        {
            println("Break!");
            break;
        }else{
            times = times-1;
            println("Continue!");
            continue;
        }
    }
    return 0;
}

```
## 6.3. 数组测试

arr的基址是根据在它之前声明的arr2的大小决定的，在访问和更改arr的元素的时候也是如此
```c
def void main()
{
    int n,times;
    array arr2[100];
    array arr[255];
    arr[66] = 65535;
    println(arr[66]);
    
    return 0;
}
```
输出结果为65535，正确！

## 6.2. Test case:
```c
int global1,global2;

def void factor( int N ){
    global1 = global1 + 1;
    println("\nThe global value: ");
    println(global1);
	if( N > 1 ){
		return N * cal factor( N - 1 );
	}
	else{
		return 1;
	}
}

def void main()
{
    int n,times;
    array arr[255];
    global1 = 1;
    times = 6;
    while(times >= 0)
    {
        n = cal factor(times);
        println("\nResult of factor:");
        println(times);
        println(" - ");
        println(n);
        println(" - ");
        if(times == 4)
        {
            println("Break!");
            break;
        }else{
            times = times-1;
            println("Continue!");
            continue;
        }
    }
    return 0;
}
```


其他：
- 见到一个变量以后的策略：先看在全局符号阵列中寻找，看是否为全局变量，然后再在此函数的实参表中寻找，如果还是没有找到就在函数的局部声明中寻找，如果还找不到的话就报错

- 最初对数组的空间考虑分配的是：在C语言中对函数调用栈的大小有限制，所以在函数内部不能定义特别大的数组，为了解决这个缺陷，我将函数中的数组定义放在了静态变量区，当函数调用退出时释放数组的空间；实现上使用一个globalpointer，当在程序最初有int或者数组声明的时候，这个pointer就会向上拓展。当进入一个函数时将当前的globalpointer地址当做分配数组的开始地址，在使用的时候也是以这个指针为导向进行索引，在退出函数之前，不需要做任何操作，因为globalpointer的值没有改变，下一个函数中声明的数组将会保存...然而递归调用会出现数组的保存问题  
所以我决定还是将函数中的数组放到函数调用栈中，在读取localvariable的值的时候要在$sp的基础上加上数组的空间，在给变量赋值的时候也要在$sp的基础上加上数组的偏移量

- 为了将数组内元素的使用和变量ID的使用统一起来，我修改了词法分析器，使得其将数组元素的使用也看作ID的使用，而后修改loadVriable中的代码使得其可以辨识出是要加载的是普通的变量还是数组中的参数  

将数组定义时连同中括号以及内部的数字一同作为ID还可以使得变量和数组具有相同的名字而不会冲突