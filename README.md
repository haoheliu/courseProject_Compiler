CourseProject - Compiler

<u>Using java to implement a simple compiler</u>

<!-- TOC -->
autoauto- [1. Overall mindmap](#1-overall-mindmap)auto- [2. Syntax support](#2-syntax-support)auto    - [2.1. 算术语法](#21-算术语法)auto        - [2.1.1. calculations(- +,-,*,/)](#211-calculations---)auto        - [2.1.2. boolean expressions (and,or)](#212-boolean-expressions-andor)auto        - [2.1.3. comparision (>,<,<=,>=,!=,==)](#213-comparision-)auto        - [2.1.4. 赋值语句](#214-赋值语句)auto    - [2.2. 选择和分支](#22-选择和分支)auto        - [2.2.1. if-else](#221-if-else)auto        - [2.2.2. switch(尚未实现)](#222-switch尚未实现)auto        - [2.2.3. while](#223-while)auto        - [2.2.4. 全局变量](#224-全局变量)auto        - [2.2.5. const常量](#225-const常量)auto        - [2.2.6. function Defination](#226-function-defination)auto        - [2.2.7. functioncall](#227-functioncall)auto        - [2.2.8. goto & dest](#228-goto--dest)auto    - [2.3. Built-in functions](#23-built-in-functions)auto        - [2.3.1. 标准输出](#231-标准输出)auto        - [2.3.2. 标准输入 (尚未实现)](#232-标准输入-尚未实现)auto    - [2.4. 字符串与转义字符](#24-字符串与转义字符)auto    - [2.5. Comment](#25-comment)auto    - [2.6. Error report](#26-error-report)auto        - [2.6.1. 一个通用的报错函数](#261-一个通用的报错函数)auto        - [2.6.2. 局部符号表FuncSymTab的报错](#262-局部符号表funcsymtab的报错)auto        - [2.6.3. 全局符号表的报错](#263-全局符号表的报错)auto        - [2.6.4. 寄存器分配溢出错误](#264-寄存器分配溢出错误)auto- [3. 一些实现细节 (见代码注释)](#3-一些实现细节-见代码注释)auto    - [3.1. 函数内部定义的变量在load和save的时候如何在内存中定位](#31-函数内部定义的变量在load和save的时候如何在内存中定位)auto    - [3.2. 被调用者保存寄存器的实现](#32-被调用者保存寄存器的实现)auto    - [3.3. 函数调用直接用于算术表达式](#33-函数调用直接用于算术表达式)auto    - [3.4. 符号表设计（全局符号表和局部符号表）](#34-符号表设计全局符号表和局部符号表)auto    - [3.5. 局部变量与局部数组同时存在时如何正确定位数组基址](#35-局部变量与局部数组同时存在时如何正确定位数组基址)auto    - [3.6. 寄存器分配复位的时机](#36-寄存器分配复位的时机)auto    - [3.7. 各种变量和数组的load、save逻辑](#37-各种变量和数组的loadsave逻辑)auto- [4. 文法设计](#4-文法设计)auto    - [4.1. 上层文法：程序，函数定义，全局声明](#41-上层文法程序函数定义全局声明)auto    - [4.2. 中层文法：不同种类的statements](#42-中层文法不同种类的statements)auto    - [4.3. 中层文法细化->非终结符的产生式](#43-中层文法细化-非终结符的产生式)auto    - [4.4. 低层文法：布尔表达式，算术表达式](#44-低层文法布尔表达式算术表达式)auto- [5. Problem encountered](#5-problem-encountered)auto    - [5.1. 函数调用栈设计](#51-函数调用栈设计)auto    - [5.2. 寄存器分配设计](#52-寄存器分配设计)auto- [6. Test cases:](#6-test-cases)auto    - [6.1. Recursive function calling & breakStatement &continue Statement & globalStatement](#61-recursive-function-calling--breakstatement-continue-statement--globalstatement)auto    - [6.2. 数组测试](#62-数组测试)auto    - [6.3. Test case:](#63-test-case)auto    - [6.4. 全局数组测试](#64-全局数组测试)autoauto
<!-- /TOC -->

# 1. Overall mindmap 思路
词法分析器->语法分析器（递归下降，语法制导翻译）->寄存器分配和代码优化->MIPS指令
---

# 2. Syntax support 支持的语法

## 2.1. alrithmetic 算术语法
### 2.1.1. calculations(- +,-,*,/)
- 遵循四则运算的算术优先级，也可以使用括号来改变算术优先级

### 2.1.2. boolean expressions (and,or)
- and相当于C语言中的:&&
- or相当于C语言中的:||
- 支持短路 -> *尚未实现*

### 2.1.3. comparision (>,<,<=,>=,!=,==)
- 与正常C语言含义相同
- 可以将多个比较运算符使用and和or进行连接

### 2.1.4. 赋值语句
语法：
```c
ARRAY[n]|INTEGER = EXPRESSION
```
- 将每一次赋值语句作为寄存器的一次使用周期，在每一次赋值语句结束之后就可以将他们复位一下，重新从$t0,$s0...开始分配
- 赋值之前将左值分配寄存器，从内存中取出，存入寄存器，经过计算之后再存入内存
- 赋值语句的寄存器使用效率可能较低，而且每一次使用变量都要访存，可能会影响程序的运行速度

## 2.2. 选择和分支
### 2.2.1. if-else
语法：
```c
   if(BOOLEANEXPRESSION)
   {
         [statementList]...
   }else
   {
         [statementList]...
   }
```
### 2.2.2. switch(尚未实现)
### 2.2.3. while
语法
```c
   ...
   while(BOOLEANEXPRESSION)
   {
      [StatementList]...
   }
   ...
```
- break:正常的跳出语句
- continue：接着进行循环
### 2.2.4. 全局变量
语法：
```c
//只能在函数定义之前进行声明
int global_var1,...;
int global_var2,...;
...
array global_arr1[LENGTH1];
array global_arr2[LENGTH2];
...
```
- 全局int变量
- 全局array变量 
- 全局变量存储在内存的堆区，使用$gp指针进行检索  

### 2.2.5. const常量

### 2.2.6. function Defination
语法：
```c
def void/int <FUNCTIONNAME>(int PARAM1,int PARAM2,...)  
{
   int var1,var2,...;  
   array arr1[LENGTH1];
   array arr2[LENGTH2];
   ...
   [StatementList]...
   [returnStatement]...
}
```
- 局部变量声明：可以声明局部int变量和array变量
- 支持入口参数，在调用时使用$a0~$a3存放
- 支持返回值，返回值存放在$v0寄存器中
- 支持递归调用过程中寄存器保存，如：
```c
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
```
- 函数内部可以使用全局int 和 array变量


### 2.2.7. functioncall 
语法：
```c
cal <FUNCTIONNAME>(PARAM1,PARAM2,PARAM3...)  
```
- 支持立即数，int和array变量作为实参
- 支持将函数调用作为算术表达式的一个项（必须带返回参数）


### 2.2.8. goto & dest
语法：
```c
dest LABEL
...
goto LABEL
...
```
- 遇到dest语句就在mips中加入一个标签，执行到goto的话就用无条件跳转"j"来跳到标签处
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

## 2.3. Built-in functions
### 2.3.1. 标准输出
语法：
```c
println(<STRING>|VARIABLE|CONSTANT);
```
- 可以用来打印字符串，指向字符串的变量(打印出来的也是字符串)，变量和立即数
- 默认并没有在字符串末尾加上'\n'，需要换行的话需要在字符串中手动加入
例子：
```c
def void main()
{
   int test_var;
   array test[10];
   //字符串输出
   println("This is \t standard \\\"output\"!\n");
   test_var = 66;
   test[4]=  88;
   // Output of variables
   println(test_var);
   println(test[4]);
   //Output of constants
   println(65535);
}
```
### 2.3.2. 标准输入 (尚未实现)

## 2.4. 字符串与转义字符
- 字符串中如果识别到连续的偶数个反斜线(\)加上一个双引号(")，则这里的双引号代表字符串的结束
- 字符串中如果识别到连续的奇数个反斜线(\)加上一个双引号，则最后一个双引号为转义字符，不作为字符串的结束标志
- 字符串变量都作为".asciiz"类型存储在MIPS程序的.data段

## 2.5. Comment
语法：
```c
//这里是注释
```

## 2.6. Error report
### 2.6.1. 一个通用的报错函数
- 这个报错的函数定义在parser类中，由这个类使用
- TOKEN类中存有行列信息，支持报告出错的行和列，对于当前正在parse的token，如果出现错误可以取出currentToken然后在Exception中进行报告，具体函数设计如下：
```java
    private RuntimeException genEx(String errorMessage)
    {
       //errorMessage可以在不同的场景下进行不同定义
        return new RuntimeException("Encountered \"" +
                currentToken.image + "\" on line " +
                currentToken.beginLine + ", column " +
                currentToken.beginColumn + "." +
                errorMessage);
    }
```
### 2.6.2. 局部符号表FuncSymTab的报错
- 主要包括变量未定义和数组未定义的错误
```java
public void arrEnter(String name,int space)
    {
       ...
        if(index<0)
        {
            ...
        }else genDf(name);
    }

    public int arrLocate(String name)
    {
        int index = arr_names.indexOf(name);
        if(index >= 0)
        {
           ...
        }else
            throw new RuntimeException("Error: This array have not been defined!");
    }

    //on parsing: enter the args
    public void argEnter(String arg)
    {
        int index = args.indexOf(arg);
        if(index<0)
        {
           ...
        }
        else genDf(arg);
    }

    public void varEnter(String var)
    {
        if(args.indexOf(var)>=0)genDf(var);
        int index = vars.indexOf(var);
        if(index<0)
        {
           ...
        }
        else genDf(var);
    }
   //Exceptions
    private void genDf(String item)
    {
        throw new RuntimeException("\nError: "+item+" is already defined");
    }
```

### 2.6.3. 全局符号表的报错
```java
   //Global array have already been defined
   public void addGlobalArr(String name,int space)
    {
        ...
        if(index<0)
        {
            ...
        }else throw new RuntimeException("Error: global variable"+name+" have already been defined");
    }

   // Global variable have already been defined
    public void addGlobal(String s)
    {
        if(global_var.contains(s))
            throw new RuntimeException("Error: "+s+" has already been defined!");
        global_var.add(s);
    }

   // Function have already been defined
    public void enterFunc(String func_name,FuncSymTab func)
    {
        if (!func_tabs.containsKey(func_name))
            ...
        else throw new RuntimeException("Error: Function \""+func_name+"\" has already defined");
    }
```
### 2.6.4. 寄存器分配溢出错误
```java
public String registerAvailable()
    {
        String temp = "$t"+this.registerT_count++;
        //Totally we have $t0~$t9, so if we don't have enough register, we will throw an exception
        if(this.registerT_count == 11)
        {
           //register $tx not enough
            throw new RuntimeException("Temporary registor overflow");
        }
        return temp;
    }

    public String registerS_Available(){
        String temp = "$s"+this.registerS_count++;
        if(this.registerS_count == 9)
        {
           //register $sx not enough
            throw new RuntimeException("s registor overflow");
        }
        return temp;
    }

    public String registerA_Available(){
        String temp = "$a"+this.registerA_count++;
        if(this.registerA_count == 5)
        {
           //register $ax not enough
            throw new RuntimeException("Registor a overflow");
        }
        return temp;
    }
```

# 3. 一些实现细节 (详见代码注释)
## 3.1. 函数内部定义的变量在load和save的时候如何在内存中定位 
## 3.2. 被调用者保存寄存器的实现
## 3.3. 函数调用直接用于算术表达式
## 3.4. 符号表设计（全局符号表和局部符号表）
## 3.5. 局部变量与局部数组同时存在时如何正确定位数组基址
## 3.6. 寄存器分配复位的时机
## 3.7. 各种变量和数组的load、save逻辑

---

# 4. 文法设计
- 词法分析器(TokenMgr.java)因为比较简单，这里先省略
## 4.1. 上层文法：程序，函数定义，全局声明
**program** 					-&gt;**programUnitList**  &lt;EOF&gt;  
|&lt;EOF&gt;

**programUnitList**  			-&gt;**programUnit**  **programUnitList**  {“def”}  
|ε

**programUnit**  				->**functionDefinition** {“def”}  
|**globalDeclarations** {"int"}  
|ε

**globalDeclarations**			->	”int” &lt;ID&gt; **globalTail** “;”  **globalDeclarations**  
|"array" &lt;ID> "[" &lt;UNSIGNED> "]" ";" **globalDeclarations**    
|const &lt;ID> "=" &LT;UNSIGNED> ";" **globalDeclarations**   
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
## 4.2. 中层文法：不同种类的statements
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
## 4.3. 中层文法细化->非终结符的产生式
**assignmentAndBoolen**		-&gt;	&lt;ID&gt;  **assignmentStatement**

**assignmentStatement**		-&gt;	”=”  **expr**  ”;”

**argumentList**				-&gt;	**expr**  **argtail**  
|ε{“)”} {&lt;UNSIGNED&gt;,”+”,”-”,&lt;ID&gt;,”(”,&lt;STRING&gt;}

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
## 4.4. 低层文法：布尔表达式，算术表达式
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

# 5. Key problems 重点问题
## 5.1. Design of function calling stack 函数调用栈设计

函数调用栈 | 备注 |指针
-|-|-
$a0|
...|从$a0~$a3，在函数调用之前先将实参压栈
$ra|存储函数返回地址
$fp|存储旧的$fp指针值|<- $fp
int变量1|
int变量2|
...|int变量声明
array_1分配的空间|
...|
array_2分配的空间|
...|
array_n分配的空间|
...|
调用者保存的$sx寄存器|
...|
调用者保存的$tx寄存器|
...|在函数递归调用时需要由被调用者保存这些寄存器到栈，返回时将这些变量返回到寄存器中
栈顶||<- $sp



## 5.2. Strategy for register management 寄存器分配设计


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
## 6.2. 数组测试

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

## 6.3. 递归调用寄存器保存测试:
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
## 6.4. 全局数组测试
```c
array arr1[80];

def void test()
{
    println(arr1[66]);
}

def void main()
{
    arr1[66] = 65535;
    cal test()
    return 0;
}
```

其他：
- 见到一个变量以后的策略：先看在全局符号阵列中寻找，看是否为全局变量，然后再在此函数的实参表中寻找，如果还是没有找到就在函数的局部声明中寻找，如果还找不到的话就报错

- 最初对数组的空间考虑分配的是：在C语言中对函数调用栈的大小有限制，所以在函数内部不能定义特别大的数组，为了解决这个缺陷，我将函数中的数组定义放在了静态变量区，当函数调用退出时释放数组的空间；实现上使用一个globalpointer，当在程序最初有int或者数组声明的时候，这个pointer就会向上拓展。当进入一个函数时将当前的globalpointer地址当做分配数组的开始地址，在使用的时候也是以这个指针为导向进行索引，在退出函数之前，不需要做任何操作，因为globalpointer的值没有改变，下一个函数中声明的数组将会保存...然而递归调用会出现数组的保存问题  
所以我决定还是将函数中的数组放到函数调用栈中，在读取localvariable的值的时候要在$sp的基础上加上数组的空间，在给变量赋值的时候也要在$sp的基础上加上数组的偏移量

- 为了将数组内元素的使用和变量ID的使用统一起来，我修改了词法分析器，使得其将数组元素的使用也看作ID的使用，而后修改loadVriable中的代码使得其可以辨识出是要加载的是普通的变量还是数组中的参数  

- 将数组定义时连同中括号以及内部的数字一同作为ID还可以使得变量和数组具有相同的名字而不会冲突