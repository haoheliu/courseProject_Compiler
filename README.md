**西北工业大学《编译原理》试点班作品 - 林奕老师**

# 1. Overview 概述

## 1.1 整体思路

词法分析器->语法分析器（递归下降，语法制导翻译）->寄存器分配和代码优化->MIPS汇编指令集

## 1.2 特色

- 按照软件工程和设计模式规范开发，文档完备
- 使用github进行项目管理，各个版本清晰可见：<https://github.com/Ranchofromxgd/courseProject_Compiler>
- 文法由自己设计，实现了所有C0的语法，并有适当拓展
- 代码使用工业级编码规范进行书写，符合java设计模式要求
- 代码内文档详尽，注释清晰，可读性和可维护性强
- 注重细节，对源代码格式鲁棒性高（例如循环递归调用的寄存器保存）
- 生成的汇编代码自带注释，可读性强（见文档最后附录编译结果）
- 使用自己编写的assert函数进行集成测试，不通过时会使用exit系统调用退出
- 寄存器分配和符号表结构设计高效合理，算法设计效率高

## 1.3 总体设计

![ZCyn4s.png](https://s2.ax1x.com/2019/06/23/ZCyn4s.png)

## 1.4 开发环境

**IntelliJ IDEA 2019.1**

**JRE:1.8.0_202-RELEASE-1483-B39 AMD64**

**JVM: OpenJDK 64-Bits Server VM by JetBrains**

# 2. Syntax support 支持的语法

## 2.1. alrithmetic 算术语法

### 2.1.1. calculations(- +,-,*,/)

- 遵循四则运算的算术优先级，也可以使用括号来改变算术优先级

### 2.1.2. boolean expressions (and,or)

- and相当于C语言中的:&&
- or相当于C语言中的:||
- 支持短路

### 2.1.3. comparision (>,<,<=,>=,==)

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

### 2.2.2. while

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

### 2.2.3. 全局变量

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

### 2.2.4. function Defination

语法：

```c
def void/int <FUNCTIONNAME>(int PARAM1,int PARAM2,...)  
{
   int var1,var2,...;  
   array arr1[LENGTH1];
   array arr2[LENGTH2];
   ...
   const int c1 = 123;
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

### 2.2.5. const常量

语法：

```c
const int <ID> = EXPR;
```

- CONST声明过的变量在试图修改值(saveVariable())的时候会报错

### 2.2.6. functioncall 

语法：

```c
cal <FUNCTIONNAME>(PARAM1,PARAM2,PARAM3...)  
```

- 支持立即数，int和array变量作为实参
- 支持将函数调用作为算术表达式的一个项（必须带返回参数）

### 2.2.7. goto & dest

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

## 2.3. 强制退出语句

语法：

```c
exit;       //force quit
```

## 2.4. Built-in functions 内建函数

### 2.4.1. 标准输出

语法：

```c
println(<STRING>|VARIABLE|CONSTANT);
```

- 可以用来打印字符串，指向字符串的变量(打印出来的也是字符串)，变量和立即数
- 默认在字符串末尾加上一个换行符，这个通过修改StringMgr中的Enter函数实现
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

### 2.4.2. 标准输入

语法：

```java
int readint()
```

- 调用这个函数的返回值为int型

## 2.5. 字符串与转义字符

- 字符串中如果识别到连续的偶数个反斜线(\)加上一个双引号(")，则这里的双引号代表字符串的结束
- 字符串中如果识别到连续的奇数个反斜线(\)加上一个双引号，则最后一个双引号为转义字符，不作为字符串的结束标志
- 字符串变量都作为".asciiz"类型存储在MIPS程序的.data段

## 2.6. assert 语句

语法：

```java
assert(EXPR1 == EXPR2);
```

如果两个expr的值不相等，程序会调用exit自动退出

## 2.7. Comment

语法：

```c
//这里是注释
```

## 2.8. Error report

### 2.8.1. 一个通用的报错函数

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

### 2.8.2. 局部符号表FuncSymTab的报错

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

### 2.8.3. 全局符号表的报错

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

### 2.8.4. 寄存器分配溢出错误

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

# 3. Details 一些实现细节 (详见代码注释)

## 3.1. 函数内部定义的变量在load和save的时候如何在内存中定位 

详见Parser.java中loadVariable函数，注释非常详尽：

```java
/**
     * This function is defined in order to unify the "load" operation from local variables and global varibales
     * */
    private void loadVariable(String reg,String var)
    {
        int index;
        /***************
         * First consider whether this variable is an array
         * Then we need some code to loadvariable into register
         * ********/
        if(var.indexOf('[') > 0 && var.indexOf(']') > 0)
        {
            String name = var.substring(0, var.indexOf('['));
            int arr_index = Integer.parseInt(var.substring(var.indexOf('[')+1, var.indexOf(']')));
            /**
             * First consider if it's a global array
             * */
            int offset = st.locateGlobalArr(name);
            if(offset < 0)
            {
                /**
                 * If not global array, see if it's an array defined within function
                 * */
                offset = ft.getOffset(name, ARRAY);
                emitInstruction("lw", reg,offset+arr_index*4+"($sp)");
            }else
            {
                int base = st.getGlobalVarSize(); //Start from the last item of global variables
                emitInstruction("lw",reg,base+offset+arr_index*4+"($gp)");
            }
//            System.out.println("String name:"+name);
//            System.out.println("String name:"+offset);
        }else {
            /**
             * If not array, first consider if it's global variable
             * */
            index = st.locateGlobal(var);
            System.out.println("index of the globle variable: "+index);
            if(index >= 0) // If this variable is found in global variable list
            {
                emitInstruction("lw",reg,(index*4)+"($gp)");
                return;
            }
            /**
             * If not global variables, consider if it's local variable
             * */
            index = ft.getOffset(var, ARGS);
            if(index >= 0)
            {
                //If this variable is defined in args list
                /**@marked
                 * The push sequence and index sequence are inverse
                 * So we need some tricks
                 * */
                emitInstruction("lw",reg, index+"($fp)");
            }
            else if(index < 0)
            {
                //If this variable is defined in local variables list
                index = ft.getOffset(var,INT);
                if(index >= 0)emitInstruction("lw",reg, index+"($sp)");
            }
            /**
             * If not local variables, consider if it's const variable
             * */
            if(index <0)
            {
                index = ft.getOffset(var, CONST);
                if(index >= 0)emitInstruction("lw", reg,index+"($sp)");
            }

            if(index < 0) throw genEx(var+" not defined");
        }
    }
```



## 3.2. 被调用者保存寄存器的实现

在Parser.java的functionCall()函数中有这么一段代码用来识别需要保存的寄存器，并在函数退出之后恢复这些寄存器，并且将这些寄存器按照压栈顺序反向依次弹栈：

```java
//argumentList will also consume registers
        int reg_t = rm.registerT_count;
        int reg_s = rm.registerS_count;
        int save_reg = (-4)*(reg_s+reg_t);

        System.out.println("reg"+rm.registerT_count);
        System.out.println("reg"+rm.registerS_count);

        emitInstruction("addi", "$sp","$sp",""+save_reg,"# "+(reg_t+reg_s)+" registers need to be saved");

        int pointer = -save_reg-4;
        for(int i=reg_s-1;i>=0;i--)
        {
            emitInstruction("sw", "$s"+i,pointer+"($sp)");
            pointer-=4;
        }
        for(int i=reg_t-1;i>=0;i--)
        {
            emitInstruction("sw", "$t"+i,pointer+"($sp)");
            pointer-=4;
        }

        //ft.base_offset += (reg_s+reg_t)*4;

        rm.resetRegister();

        outFile.println("# Execute function: "+func_name);
        emitInstruction("jal",func_name);

        pointer = 0;

        //Reload the saved registers' value into the previous registers
        for(int i=0;i<reg_t;i++)
        {
            emitInstruction("lw", "$t"+i,pointer+"($sp)");
            pointer+=4;
        }

        for(int i=0;i<reg_s;i++)
        {
            emitInstruction("lw", "$s"+i,pointer+"($sp)");
            pointer+=4;
        }

        //ft.base_offset = 0;

        emitInstruction("addi", "$sp","$sp",""+(-1)*save_reg,"#Saved "+(reg_s+reg_t)+" registers pop stack");
    
```



## 3.3. 函数调用直接用于算术表达式

详见文法设计部分，将函数调用语句当做一个变量处理

## 3.4. 符号表设计（全局符号表和局部符号表）

详见5.2节符号表设计

## 3.5. 局部变量与局部数组同时存在时如何正确定位数组基址

在函数定义中遇到一个变量或者数组的时候，将其类型和占用空间（size）先存入局部符号表中，在整个函数parse完毕之后利用各个变量的size信息计算出各个变量以及数组基址相对于$sp指针的偏移量，并更新vars类对象的offset属性，在访问局部变量的时候利用这些信息进行定位

详见5.2节局部符号表设计

## 3.6. 寄存器分配复位的时机

- 使用的寄存器有$t0~$t9,$s0~$s7,$a0~$a3

- 每个寄存器都是从0号开始分配，如果没有足够的寄存器则报错，分配代码如下：

  ```java
      public String registerAvailable()
      {
          String temp = "$t"+this.registerT_count++;
          //Totally we have $t0~$t9, so if we don't have enough register, we will throw an exception
          if(this.registerT_count == 11)
          {
              throw new RuntimeException("Temporary registor overflow");
          }
          return temp;
      }
  
      public String registerS_Available(){
          String temp = "$s"+this.registerS_count++;
          if(this.registerS_count == 9)
          {
              throw new RuntimeException("s registor overflow");
          }
          return temp;
      }
  
      public String registerA_Available(){
          String temp = "$a"+this.registerA_count++;
          if(this.registerA_count == 5)
          {
              throw new RuntimeException("Registor a overflow");
          }
          return temp;
      }
  ```

- 在合适的时机可以reset所有寄存器的分配

  ```java
  public void resetRegister()
  {
      this.registerS_count = 0;
      this.registerT_count = 0;
      this.registerA_count = 0;
  }
  ```

- 具体的复位时机：
  - 在functioncall的jal之前（由于输入参数可能是一个复杂的表达式，占用了相当一部分寄存器）
  - 在赋值语句结束之后（赋值语句中使用到的寄存器和下一条语句没有关系）
  - 在println语句结束之后（println里边可能有复杂的表达式计算）
  - 在布尔表达式运算完毕之后（多个布尔表达式用&&或者||连接时，如果计算完一部分不reset一下，很有可能会溢出。采取的对策是，每计算完一个单独的表达式，就释放所有使用的寄存器，而且将结果存入复位后新分配的寄存器）

## 3.7. 各种变量和数组的load、save逻辑

这里以局部符号表里的变量相对于$sp寄存器作为指针的offset为例来说明变量如何在函数调用栈中进行定位，全局符号表类似，只不过使用的是$gp寄存器作为指针

分这三步：

1. 见到变量：使用Enter()收入局部符号表，存入type，size信息

2. 整个函数的变量扫描完后调用initCalBasementValue()函数来计算出各个变量相对于$sp的offset

3. 使用变量时调用getOffset()来获取offset

   代码如下：

   对于Enter，需要分为const，int和array分别来考虑

   ```java
       /**
        * Enter for ARRAY
        * */
       public void Enter(String name,int type,int size)
       {
           if(type == ARRAY)
           {
               Var temp = new Var(name,type,size);
               temp.name = temp.name.substring(0, temp.name.indexOf('['));
               if(vars.indexOf(temp) < 0){
                   this.vars.add(temp);
               }else{
                   throw new RuntimeException("Error: ["+name+"] Array has already been defined");
               }
           }else throw new RuntimeException("Error: The function \"Enter\" used is not compatible");
       }
       /**
        * Enter for INT and CONST
        * */
       public void Enter(String name,int type)
       {
           if(type == INT || type == CONST || type == ARGS)
           {
               Var temp = new Var(name,type);
               if(vars.indexOf(temp) < 0)
               {
                   this.vars.add(temp);
                   //Update the number of localVariables as well as args
                   if(type == INT) this.local_var_num ++;
                   if(type == ARGS)this.local_args_num++;
                   if(type == CONST)this.local_const_num++;
               }else{
                   throw new RuntimeException("Error: ["+name+"] Variable has already been defined");
               }
           }else throw new RuntimeException("Error: The function \"Enter\" used is not compatible");
       }
   ```

   利用变量的size信息反向计算出各个变量的offset

```java
    /**
     * @LastStepOfFuncSymTab
     * The initial value in "Var" is their size
     * After this function we will have it's offset from base_pointer
     * */
    public void initCalBasementValue()
    {
        if(vars.size() == 0)
        {
            return;
        }
        int offset = 0;
        Var temp = new Var();
        for(int i=vars.size();i>0;i--)
        {
            temp = vars.get(i-1); //Get the i-th element
            temp.offset = offset; //update it's offset
            offset += temp.size;
            vars.set(i-1, temp);  //Update this element to : vars
        }

        int j=1;
        temp = vars.get(0);

        while(temp.type == ARGS){
            try {
                temp = vars.get(j);
            }catch (Exception e){
                break;
            }
            j++;
        }
        /**
         * This value is extremely important to the pop of function stack
         * */
        if(temp.type == ARGS)
            this.space = this.local_args_num*4+8; //The space used by array,const and int
        else
            this.space = temp.size+temp.offset+this.local_args_num*4+8;

        int start_offset_fp = 8;
        for(int i=0;i<vars.size();i++)
        {
            temp = vars.get(i); //Get the i-th element
            if(temp.type == ARGS){
                temp.offset = start_offset_fp; //update it's offset
                start_offset_fp += 4;
            }
            else break;
            vars.set(i, temp);  //Update this element to : vars
        }
    }
```

获取某个变量的offset

```java
    /**
     * Return offset value for array,int and const
     * */
    public int getOffset(String name, int type)
    {
        int offset = 0;
        Var temp = new Var();
        temp.name = name;
        temp.type = type;
        int index = this.vars.indexOf(temp);
        if(index >= 0)
        {
            temp = vars.get(index);
            offset = temp.offset;
            return offset+this.base_offset;
        }
        else return -1;
    }
```



------

# 4. Grammars 文法设计

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
|"const" "int" &lt;ID> "=" expr ";"  **localDeclarations*
|ε{&lt;ID&gt;,”println”,”{”,”while”,”if”,”return”,”cal”}

**localTail** 					-&gt;	”,” &lt;ID&gt; **localTail**  
|ε {”;”}

------

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

**statement**               ->**exitStatement** {"exit"} 

**statement** 					-&gt; 	**functionCall** {“cal ”}

**statement**               -> **assertStatement** {"assert"}

------

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

**assertStatement**     -> "assert" "(" **expr** "," **expr** ")" ";"

------

## 4.4. 低层文法：布尔表达式，算术表达式

**expr**							-&gt;	term termList
{&lt;UNSIGNED&gt;,”+”,”-”,&lt;ID&gt;,”(”,&lt;STRING&gt;}

**termList**						-&gt;	”+”  term termList  
|”-” **term** **termlist**  
|”==”  **expr**    **boolenExpression**  {“and”,”or”,“)”,”;”,”,”}  
|”&gt;=”  **expr**    **boolenExpression**  
|”&lt;=”  **expr**  **boolenExpression**  
|”&gt;”  **expr**  **boolenExpression**  
|”&lt;”  **expr**  **boolenExpression**  
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

函数调用栈压栈顺序可以用下边表格表示：

| 函数调用栈            | 备注                                                         | 指针   |
| --------------------- | ------------------------------------------------------------ | ------ |
| $a0                   |                                                              |        |
| ...                   | 从$a0~$a3，在函数调用之前先将实参压栈                        |        |
| $ra                   | 存储函数返回地址                                             |        |
| $fp                   | 存储旧的$fp指针值                                            | <- $fp |
| int变量1              |                                                              |        |
| int变量2              |                                                              |        |
| ...                   | int变量声明                                                  |        |
| array_1分配的空间     |                                                              |        |
| ...                   |                                                              |        |
| array_2分配的空间     |                                                              |        |
| ...                   |                                                              |        |
| array_n分配的空间     |                                                              |        |
| ...                   |                                                              |        |
| 调用者保存的$sx寄存器 |                                                              |        |
| ...                   |                                                              |        |
| 调用者保存的$tx寄存器 |                                                              |        |
| ...                   | 在函数递归调用时需要由被调用者保存这些寄存器到栈，返回时将这些变量返回到寄存器中 |        |
| 栈顶                  |                                                              | <- $sp |

## 5.2 符号表设计

- 全局符号表用来储存全局int变量以及全局数组，存储在静态数据区，使用$gp指针进行访问
- 全局符号表中用一个哈希表来对函数名和局部符号表进行一一映射
- 其他设计设计细节如图所示：

[![ZS7RWF.png](https://s2.ax1x.com/2019/06/21/ZS7RWF.png)](https://imgchr.com/i/ZS7RWF)

## 5.3. Strategy for register management 寄存器分配设计

- 寄存器分配管理由类**RegMgr**进行
- 使用的寄存器组：$t0~$t9,$s0~$s7,$a0~$a3，每次请求寄存器成功后就将寄存器号+1，如果超出了现有的寄存器数量，则报错寄存器分配溢出
- 注意到一条赋值语句结束之后，使用过的寄存器就可以全部清空了，在本文档中定义这种现象为**前向无依赖性**，具有这种性质的语句结束后都可以调用rm.reset()方法来复位寄存器；对于形如expr1 && expr2 || expr3... 这样的布尔表达式，如果表达式比较长而且参与计算的变量或者常量比较多，非常容易产生寄存器的分配溢出，为了防止这种现象的出现， 每计算完一个expr的布尔值，就释放掉所有expr使用过的寄存器，并将计算结果放入一个重新分配的寄存器中。使用这种方法平均下来每个布尔表达式只需要1个寄存器，大大消除了寄存器分配溢出的风险。
- 清空的方法：下次分配寄存器的时候从$t0，$s0开始分配

# 6. Test cases一部分测例:

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

- 测试函数内部数组：

```c
//Function call need not ';'
def void main(int argtest)
{
    array s[100];
    int a;              //test whether the offset is set correctly
    array s2[4];
    s2[1] = 100;
    //LOCAL TEST
    //a = 456;    
    //s[3] = 655*a + s2[1] ;  //test the load and save of array element
    //ARGS TEST
    argtest = 456;    
    s[3] = 655*argtest + s2[1] ;  //test the load and save of array element
    println(s[3]);     
}
```

## 6.5  集成测试

通过这个测例可以测试几乎所有的实现的语法，使用assert语句进行判断程序运行的对错，如果assert失败，则会结束程序的运行，使用系统调用exit方法强制结束程序运行

```c
//SOME GLOBAL VARIABLES
int test1,test2;
array arr1[4];
array arr2[8];

def void modifyGlobal()
{
    test1 = 111;
    test2 = 222;
    arr1[3] = 65535;
    arr2[1] = 100;
}

def int Fabio(int n)    //recursive
{
    array retval[2];
	if(n == 1 or n ==2)
	{
		return 1;
	}
	else
	{
	    retval[0] = cal Fabio(n-1);
	    retval[1] = cal Fabio(n-2);

		return retval[0]+retval[1];
	}
}

def int factor( int N ){
	if( N > 1 ){
		return N * cal factor( N - 1 );
	}
	else{
		return 1;
	}
}

def void main(int argtest,int argtest2)
{
                                                //ARRAY TEST
    array s[100];
    int a,times;              //test whether the offset is set correctly
    int result,start,end;           //For the test of Fibonacci sequence
    int short1,short2,short3;
    array s2[4];
    const int test = 50+8*(66+3);
    //START TEST!!!
    println("******************Compiler naive****************");
    println("*****************author:Haohe Liu***************");
    println("*******************START TEST*******************");
    println("----------------1.Array test----------------");

    s2[1] = 456;
    assert(s2[1], 456);
    a = 543;
    assert(a, 543);
    s[3] = 655*a + s2[1];    //test the load and save of array element
    assert(s[3], 356121);
    argtest = 100;
    assert(argtest, 100);
    a = 1+(2+4)*100;
    assert(a,601);
    println("PASS");
                                                //RECURSIVE TEST
    println("----------------2.recursive test------------");

    println("=========factorial test=========");
    times = 6;
    a = cal factor(times);
    println("\tResult of recursive test:\n\t");
    println(a);
    println("PASS");

    println("=========Fibonacci sequence test=========");
    start = 1;
    end = 10;
    println("\tFibonacci sequence calculated by recursion from 1 to 10");
    while(start <= end)
    {
        result = cal Fabio(start);
        println(result);
        start = start + 1;
    }
    println("PASS");
                                                //WHILE TEST
    println("----------------3.while test----------------");
    println("\tCount down from 10 to 5:");
    times = 10;
    while(times >= 0)
    {
        println(times);
        if(times == 4){
            break;
        }
        times  = times -1;
    }
    println("PASS");
                                                //IF TEST
    println("----------------4.if test----------------");
    println("\tValue of start: \n\t");
    println(start);
    println("\tValue of end \n\t");
    println(end);
    if(start == 1 or end == 10){
        println("\tstart == 1 or end == 10");
    }
    if(start < 1 or end > 10)
    {
        println("\tstart < 1 or end > 10");
    }else
    {
        println("\tstart >= 1 or end <= 10");
    }

    println("PASS");
                                                //GOTO TEST
    println("----------------5.goto test----------------");
    println("\tStart of goto test, you will see nothing if it works");
    goto end;
    println("\tIf you see this, it means you are wrong!");
    dest end;
    println("PASS");
                                                //SHORT CIRCUIT TEST
    println("----------------6.short circuit test----------------");
    short1 = 10;
    short2 = 100;
    short3 = 1000;
    assert(short3,1000);
    while(short1 == 10 and short2 <= 99 and short3 > 900)
    {
        println("\tShort circuit!");
        break;
    }
    println("PASS");
                                                //CONST TEST

    println("----------------7.constant modification test----------------");
    println("!attension: in order to perform this test, please modify the original code");
    //test = 100;  //Const variable cannot be modified
    println("PASS");

    println("----------------8.global modification test----------------");
    println("\tBefore modification:");
    println(test1);
    println(test2);
    println(arr1[3]);
    println(arr2[1]);
    cal modifyGlobal()   //Call this function to modify the global variable
    println("\tAfter modification:");
    println(test1);
    println(test2);
    println(arr1[3]);
    println(arr2[1]);
    println("PASS");

    println("----------------9.String test----------------");
    println("\tThis string has a \"double quotation\" in it");
    println("PASS");


    println("----------------10.exit test----------------");
    println("If you see nothing, then exit is success");
    println("PASS");
    exit;
    println("Wrong man!");


}
```

# 7. Result 运行结果

## 7.1 集成测试运行结果

[![ZSqqYV.png](https://s2.ax1x.com/2019/06/21/ZSqqYV.png)](https://imgchr.com/i/ZSqqYV)

```java
-- program is finished running (dropped off bottom) --

******************Compiler naive****************
*****************author:Haohe Liu***************
*******************START TEST*******************
----------------1.Array test----------------
PASS
----------------2.recursive test------------
=========factorial test=========
	Result of recursive test:
	
720
PASS
=========Fibonacci sequence test=========
	Fibonacci sequence calculated by recursion from 1 to 10
1
1
2
3
5
8
13
21
34
55
PASS
----------------3.while test----------------
	Count down from 10 to 5:
10
9
8
7
6
5
4
PASS
----------------4.if test----------------
	Value of start: 
	
11
	Value of end 
	
10
	start == 1 or end == 10
	start >= 1 or end <= 10
PASS
----------------5.goto test----------------
	Start of goto test, you will see nothing if it works
PASS
----------------6.short circuit test----------------
PASS
----------------7.constant modification test----------------
!attension: in order to perform this test, please modify the original code
PASS
----------------8.global modification test----------------
	Before modification:
0
0
0
0
	After modification:
111
222
65535
100
PASS
----------------9.String test----------------
	This string has a "double quotation" in it
PASS
----------------10.exit test----------------
If you see nothing, then exit is success
PASS

-- program is finished running --
```



## 7.2 集成测试汇编代码

```java
	.text
move	$fp,	$sp
jal	main
j	exit
addi	$gp,	$gp,	4			#Space for variable: test1
addi	$gp,	$gp,	4			#Space for variable: test2

modifyGlobal:
addi	$sp,	$sp,	-8			#Create space for $ra and $fp
sw	$ra,	4($sp)
sw	$fp,	0($sp)
move	$fp,	$sp
#Assignment statement for varaible: test1
lw	$t0,	0($gp)
li	$t1,	111
move	$t0,	$t1
sw	$t0,	0($gp)
#The end of assignment
#Assignment statement for varaible: test2
lw	$t0,	4($gp)
li	$t1,	222
move	$t0,	$t1
sw	$t0,	4($gp)
#The end of assignment
#Assignment statement for varaible: arr1[3]
lw	$t0,	20($gp)
li	$t1,	65535
move	$t0,	$t1
sw	$t0,	20($gp)
#The end of assignment
#Assignment statement for varaible: arr2[1]
lw	$t0,	28($gp)
li	$t1,	100
move	$t0,	$t1
sw	$t0,	28($gp)
#The end of assignment
#Restore register $ra and $fp
lw	$ra,	4($fp)
lw	$fp,	0($fp)
addi	$sp,	$sp,	0			#pop stack all at once
jr	$ra

Fabio:
addi	$sp,	$sp,	-12			#Create space for args ,$ra and $fp
sw	$a0,	8($sp)
sw	$ra,	4($sp)
sw	$fp,	0($sp)
move	$fp,	$sp
addi	$sp,	$sp,	-8			#Create space for :  retval[2]
lw	$t0,	8($fp)
li	$t1,	1
seq	$s0,	$t0,	$t1
lw	$t2,	8($fp)
li	$t3,	2
seq	$s2,	$t2,	$t3
or	$s0,	$s0,	$s2
beq	$zero,	$s0,	L0
li	$t0,	1
#return value of Fabio
move	$v0,	$t0
j	L1
L0:
#Assignment statement for varaible: retval[0]
lw	$t1,	0($sp)
lw	$t2,	8($fp)
li	$t3,	1
sub	$t4,	$t2,	$t3
move	$a0,	$t4
addi	$sp,	$sp,	-20			# 5 registers need to be saved
sw	$t4,	16($sp)
sw	$t3,	12($sp)
sw	$t2,	8($sp)
sw	$t1,	4($sp)
sw	$t0,	0($sp)
# Execute function: Fabio
jal	Fabio
lw	$t0,	0($sp)
lw	$t1,	4($sp)
lw	$t2,	8($sp)
lw	$t3,	12($sp)
lw	$t4,	16($sp)
addi	$sp,	$sp,	20			#Saved 5 registers pop stack
move	$t1,	$v0
sw	$t1,	0($sp)
#The end of assignment
#Assignment statement for varaible: retval[1]
lw	$t0,	4($sp)
lw	$t1,	8($fp)
li	$t2,	2
sub	$t3,	$t1,	$t2
move	$a0,	$t3
addi	$sp,	$sp,	-16			# 4 registers need to be saved
sw	$t3,	12($sp)
sw	$t2,	8($sp)
sw	$t1,	4($sp)
sw	$t0,	0($sp)
# Execute function: Fabio
jal	Fabio
lw	$t0,	0($sp)
lw	$t1,	4($sp)
lw	$t2,	8($sp)
lw	$t3,	12($sp)
addi	$sp,	$sp,	16			#Saved 4 registers pop stack
move	$t0,	$v0
sw	$t0,	4($sp)
#The end of assignment
lw	$t0,	0($sp)
lw	$t1,	4($sp)
add	$t2,	$t1,	$t0
#return value of Fabio
move	$v0,	$t2
L1:
#Restore register $ra and $fp
lw	$ra,	4($fp)
lw	$fp,	0($fp)
addi	$sp,	$sp,	20			#pop stack all at once
jr	$ra

factor:
addi	$sp,	$sp,	-12			#Create space for args ,$ra and $fp
sw	$a0,	8($sp)
sw	$ra,	4($sp)
sw	$fp,	0($sp)
move	$fp,	$sp
lw	$t3,	8($fp)
li	$t4,	1
sgt	$s0,	$t3,	$t4
beq	$zero,	$s0,	L2
lw	$t0,	8($fp)
lw	$t1,	8($fp)
li	$t2,	1
sub	$t3,	$t1,	$t2
move	$a0,	$t3
addi	$sp,	$sp,	-16			# 4 registers need to be saved
sw	$t3,	12($sp)
sw	$t2,	8($sp)
sw	$t1,	4($sp)
sw	$t0,	0($sp)
# Execute function: factor
jal	factor
lw	$t0,	0($sp)
lw	$t1,	4($sp)
lw	$t2,	8($sp)
lw	$t3,	12($sp)
addi	$sp,	$sp,	16			#Saved 4 registers pop stack
mult	$t0,	$v0
mflo	$t0
#return value of factor
move	$v0,	$t0
j	L3
L2:
li	$t1,	1
#return value of factor
move	$v0,	$t1
L3:
#Restore register $ra and $fp
lw	$ra,	4($fp)
lw	$fp,	0($fp)
addi	$sp,	$sp,	12			#pop stack all at once
jr	$ra

main:
addi	$sp,	$sp,	-16			#Create space for args ,$ra and $fp
sw	$a0,	12($sp)
sw	$a1,	8($sp)
sw	$ra,	4($sp)
sw	$fp,	0($sp)
move	$fp,	$sp
addi	$sp,	$sp,	-400			#Create space for :  s[100]
addi	$sp,	$sp,	-8			#Create space for local variables
addi	$sp,	$sp,	-20			#Create space for local variables
addi	$sp,	$sp,	-32			#Create space for local variables
addi	$sp,	$sp,	-16			#Create space for :  s2[4]
addi	$sp,	$sp,	-4			#Create space for const variable: test
li	$t2,	66
li	$t3,	3
add	$t4,	$t3,	$t2
li	$t5,	8
mult	$t5,	$t4
mflo	$t6
li	$t7,	50
add	$t8,	$t6,	$t7
sw	$t8,	0($sp)
#println Statement
la	$t0,	Str1
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
la	$t0,	Str2
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
la	$t0,	Str3
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
la	$t0,	Str4
li	$v0,	4
move	$a0,	$t0
syscall
#Assignment statement for varaible: s2[1]
lw	$t0,	8($sp)
li	$t1,	456
move	$t0,	$t1
sw	$t0,	8($sp)
#The end of assignment
# Assert statement
lw	$t0,	8($sp)
li	$t1,	456
seq	$s0,	$t0,	$t1
beq	$zero,	$s0,	L5			#If not equal, exit the hole program
j	L4
L5:
li	$v0,	10
syscall
L4:
#Assignment statement for varaible: a
lw	$t0,	48($sp)
li	$t1,	543
move	$t0,	$t1
sw	$t0,	48($sp)
#The end of assignment
# Assert statement
lw	$t0,	48($sp)
li	$t1,	543
seq	$s0,	$t0,	$t1
beq	$zero,	$s0,	L7			#If not equal, exit the hole program
j	L6
L7:
li	$v0,	10
syscall
L6:
#Assignment statement for varaible: s[3]
lw	$t0,	64($sp)
lw	$t1,	48($sp)
li	$t2,	655
mult	$t2,	$t1
mflo	$t3
lw	$t4,	8($sp)
add	$t5,	$t4,	$t3
move	$t0,	$t5
sw	$t0,	64($sp)
#The end of assignment
# Assert statement
lw	$t0,	64($sp)
li	$t1,	356121
seq	$s0,	$t0,	$t1
beq	$zero,	$s0,	L9			#If not equal, exit the hole program
j	L8
L9:
li	$v0,	10
syscall
L8:
#Assignment statement for varaible: argtest
lw	$t0,	8($fp)
li	$t1,	100
move	$t0,	$t1
sw	$t0,	8($fp)
#The end of assignment
# Assert statement
lw	$t0,	8($fp)
li	$t1,	100
seq	$s0,	$t0,	$t1
beq	$zero,	$s0,	L11			#If not equal, exit the hole program
j	L10
L11:
li	$v0,	10
syscall
L10:
#Assignment statement for varaible: a
lw	$t0,	48($sp)
li	$t1,	2
li	$t2,	4
add	$t3,	$t2,	$t1
li	$t4,	100
mult	$t3,	$t4
mflo	$t5
li	$t6,	1
add	$t7,	$t5,	$t6
move	$t0,	$t7
sw	$t0,	48($sp)
#The end of assignment
# Assert statement
lw	$t0,	48($sp)
li	$t1,	601
seq	$s0,	$t0,	$t1
beq	$zero,	$s0,	L13			#If not equal, exit the hole program
j	L12
L13:
li	$v0,	10
syscall
L12:
#println Statement
la	$t0,	Str5
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
la	$t0,	Str6
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
la	$t0,	Str7
li	$v0,	4
move	$a0,	$t0
syscall
#Assignment statement for varaible: times
lw	$t0,	44($sp)
li	$t1,	6
move	$t0,	$t1
sw	$t0,	44($sp)
#The end of assignment
#Assignment statement for varaible: a
lw	$t0,	48($sp)
lw	$t1,	44($sp)
move	$a0,	$t1
addi	$sp,	$sp,	-8			# 2 registers need to be saved
sw	$t1,	4($sp)
sw	$t0,	0($sp)
# Execute function: factor
jal	factor
lw	$t0,	0($sp)
lw	$t1,	4($sp)
addi	$sp,	$sp,	8			#Saved 2 registers pop stack
move	$t0,	$v0
sw	$t0,	48($sp)
#The end of assignment
#println Statement
la	$t0,	Str8
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
lw	$t0,	48($sp)
li	$v0,	1
move	$a0,	$t0
syscall
la	$t0,	Str0
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
la	$t0,	Str9
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
la	$t0,	Str10
li	$v0,	4
move	$a0,	$t0
syscall
#Assignment statement for varaible: start
lw	$t0,	36($sp)
li	$t1,	1
move	$t0,	$t1
sw	$t0,	36($sp)
#The end of assignment
#Assignment statement for varaible: end
lw	$t0,	32($sp)
li	$t1,	10
move	$t0,	$t1
sw	$t0,	32($sp)
#The end of assignment
#println Statement
la	$t0,	Str11
li	$v0,	4
move	$a0,	$t0
syscall
# WhileStatement
L14:
lw	$t0,	36($sp)
lw	$t1,	32($sp)
sle	$s0,	$t0,	$t1
beq	$zero,	$s0,	L15
#Assignment statement for varaible: result
lw	$t0,	40($sp)
lw	$t1,	36($sp)
move	$a0,	$t1
addi	$sp,	$sp,	-8			# 2 registers need to be saved
sw	$t1,	4($sp)
sw	$t0,	0($sp)
# Execute function: Fabio
jal	Fabio
lw	$t0,	0($sp)
lw	$t1,	4($sp)
addi	$sp,	$sp,	8			#Saved 2 registers pop stack
move	$t0,	$v0
sw	$t0,	40($sp)
#The end of assignment
#println Statement
lw	$t0,	40($sp)
li	$v0,	1
move	$a0,	$t0
syscall
la	$t0,	Str0
li	$v0,	4
move	$a0,	$t0
syscall
#Assignment statement for varaible: start
lw	$t0,	36($sp)
lw	$t1,	36($sp)
li	$t2,	1
add	$t3,	$t2,	$t1
move	$t0,	$t3
sw	$t0,	36($sp)
#The end of assignment
j	L14
L15:
#println Statement
la	$t0,	Str12
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
la	$t0,	Str13
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
la	$t0,	Str14
li	$v0,	4
move	$a0,	$t0
syscall
#Assignment statement for varaible: times
lw	$t0,	44($sp)
li	$t1,	10
move	$t0,	$t1
sw	$t0,	44($sp)
#The end of assignment
# WhileStatement
L16:
lw	$t0,	44($sp)
li	$t1,	0
sge	$s0,	$t0,	$t1
beq	$zero,	$s0,	L17
#println Statement
lw	$t0,	44($sp)
li	$v0,	1
move	$a0,	$t0
syscall
la	$t0,	Str0
li	$v0,	4
move	$a0,	$t0
syscall
lw	$t0,	44($sp)
li	$t1,	4
seq	$s0,	$t0,	$t1
beq	$zero,	$s0,	L18
j	L17
j	L19
L18:
L19:
#Assignment statement for varaible: times
lw	$t0,	44($sp)
lw	$t1,	44($sp)
li	$t2,	1
sub	$t3,	$t1,	$t2
move	$t0,	$t3
sw	$t0,	44($sp)
#The end of assignment
j	L16
L17:
#println Statement
la	$t0,	Str15
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
la	$t0,	Str16
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
la	$t0,	Str17
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
lw	$t0,	36($sp)
li	$v0,	1
move	$a0,	$t0
syscall
la	$t0,	Str0
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
la	$t0,	Str18
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
lw	$t0,	32($sp)
li	$v0,	1
move	$a0,	$t0
syscall
la	$t0,	Str0
li	$v0,	4
move	$a0,	$t0
syscall
lw	$t0,	36($sp)
li	$t1,	1
seq	$s0,	$t0,	$t1
lw	$t2,	32($sp)
li	$t3,	10
seq	$s2,	$t2,	$t3
or	$s0,	$s0,	$s2
beq	$zero,	$s0,	L20
#println Statement
la	$t0,	Str19
li	$v0,	4
move	$a0,	$t0
syscall
j	L21
L20:
L21:
lw	$t0,	36($sp)
li	$t1,	1
slt	$s0,	$t0,	$t1
lw	$t2,	32($sp)
li	$t3,	10
sgt	$s1,	$t2,	$t3
or	$s0,	$s0,	$s1
beq	$zero,	$s0,	L22
#println Statement
la	$t0,	Str20
li	$v0,	4
move	$a0,	$t0
syscall
j	L23
L22:
#println Statement
la	$t0,	Str21
li	$v0,	4
move	$a0,	$t0
syscall
L23:
#println Statement
la	$t0,	Str22
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
la	$t0,	Str23
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
la	$t0,	Str24
li	$v0,	4
move	$a0,	$t0
syscall
j	end
#println Statement
la	$t0,	Str25
li	$v0,	4
move	$a0,	$t0
syscall
end:
#println Statement
la	$t0,	Str26
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
la	$t0,	Str27
li	$v0,	4
move	$a0,	$t0
syscall
#Assignment statement for varaible: short1
lw	$t0,	28($sp)
li	$t1,	10
move	$t0,	$t1
sw	$t0,	28($sp)
#The end of assignment
#Assignment statement for varaible: short2
lw	$t0,	24($sp)
li	$t1,	100
move	$t0,	$t1
sw	$t0,	24($sp)
#The end of assignment
#Assignment statement for varaible: short3
lw	$t0,	20($sp)
li	$t1,	1000
move	$t0,	$t1
sw	$t0,	20($sp)
#The end of assignment
# Assert statement
lw	$t0,	20($sp)
li	$t1,	1000
seq	$s0,	$t0,	$t1
beq	$zero,	$s0,	L25			#If not equal, exit the hole program
j	L24
L25:
li	$v0,	10
syscall
L24:
# WhileStatement
L26:
lw	$t0,	28($sp)
li	$t1,	10
seq	$s0,	$t0,	$t1
lw	$t2,	24($sp)
li	$t3,	99
sle	$s2,	$t2,	$t3
lw	$t4,	20($sp)
li	$t5,	900
sgt	$s4,	$t4,	$t5
beq	$zero,	$s2,	L27			#Short circuit supported
and	$s0,	$s2,	$s4
beq	$zero,	$s0,	L27			#Short circuit supported
and	$s0,	$s0,	$s2
beq	$zero,	$s0,	L27
#println Statement
la	$t0,	Str28
li	$v0,	4
move	$a0,	$t0
syscall
j	L27
j	L26
L27:
#println Statement
la	$t0,	Str29
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
la	$t0,	Str30
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
la	$t0,	Str31
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
la	$t0,	Str32
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
la	$t0,	Str33
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
la	$t0,	Str34
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
lw	$t0,	0($gp)
li	$v0,	1
move	$a0,	$t0
syscall
la	$t0,	Str0
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
lw	$t0,	4($gp)
li	$v0,	1
move	$a0,	$t0
syscall
la	$t0,	Str0
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
lw	$t0,	20($gp)
li	$v0,	1
move	$a0,	$t0
syscall
la	$t0,	Str0
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
lw	$t0,	28($gp)
li	$v0,	1
move	$a0,	$t0
syscall
la	$t0,	Str0
li	$v0,	4
move	$a0,	$t0
syscall
addi	$sp,	$sp,	0			# 0 registers need to be saved
# Execute function: modifyGlobal
jal	modifyGlobal
addi	$sp,	$sp,	0			#Saved 0 registers pop stack
#println Statement
la	$t0,	Str35
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
lw	$t0,	0($gp)
li	$v0,	1
move	$a0,	$t0
syscall
la	$t0,	Str0
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
lw	$t0,	4($gp)
li	$v0,	1
move	$a0,	$t0
syscall
la	$t0,	Str0
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
lw	$t0,	20($gp)
li	$v0,	1
move	$a0,	$t0
syscall
la	$t0,	Str0
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
lw	$t0,	28($gp)
li	$v0,	1
move	$a0,	$t0
syscall
la	$t0,	Str0
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
la	$t0,	Str36
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
la	$t0,	Str37
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
la	$t0,	Str38
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
la	$t0,	Str39
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
la	$t0,	Str40
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
la	$t0,	Str41
li	$v0,	4
move	$a0,	$t0
syscall
#println Statement
la	$t0,	Str42
li	$v0,	4
move	$a0,	$t0
syscall
li	$v0,	10
syscall
#println Statement
la	$t0,	Str43
li	$v0,	4
move	$a0,	$t0
syscall
#Restore register $ra and $fp
lw	$ra,	4($fp)
lw	$fp,	0($fp)
addi	$sp,	$sp,	468			#pop stack all at once
jr	$ra
exit:
	.data
Str0:	.asciiz	"\n"
Str1:	.asciiz	"******************Compiler naive****************\n"
Str2:	.asciiz	"*****************author:Haohe Liu***************\n"
Str3:	.asciiz	"*******************START TEST*******************\n"
Str4:	.asciiz	"----------------1.Array test----------------\n"
Str5:	.asciiz	"PASS\n"
Str6:	.asciiz	"----------------2.recursive test------------\n"
Str7:	.asciiz	"=========factorial test=========\n"
Str8:	.asciiz	"\tResult of recursive test:\n\t\n"
Str9:	.asciiz	"PASS\n"
Str10:	.asciiz	"=========Fibonacci sequence test=========\n"
Str11:	.asciiz	"\tFibonacci sequence calculated by recursion from 1 to 10\n"
Str12:	.asciiz	"PASS\n"
Str13:	.asciiz	"----------------3.while test----------------\n"
Str14:	.asciiz	"\tCount down from 10 to 5:\n"
Str15:	.asciiz	"PASS\n"
Str16:	.asciiz	"----------------4.if test----------------\n"
Str17:	.asciiz	"\tValue of start: \n\t\n"
Str18:	.asciiz	"\tValue of end \n\t\n"
Str19:	.asciiz	"\tstart == 1 or end == 10\n"
Str20:	.asciiz	"\tstart < 1 or end > 10\n"
Str21:	.asciiz	"\tstart >= 1 or end <= 10\n"
Str22:	.asciiz	"PASS\n"
Str23:	.asciiz	"----------------5.goto test----------------\n"
Str24:	.asciiz	"\tStart of goto test, you will see nothing if it works\n"
Str25:	.asciiz	"\tIf you see this, it means you are wrong!\n"
Str26:	.asciiz	"PASS\n"
Str27:	.asciiz	"----------------6.short circuit test----------------\n"
Str28:	.asciiz	"\tShort circuit!\n"
Str29:	.asciiz	"PASS\n"
Str30:	.asciiz	"----------------7.constant modification test----------------\n"
Str31:	.asciiz	"!attension: in order to perform this test, please modify the original code\n"
Str32:	.asciiz	"PASS\n"
Str33:	.asciiz	"----------------8.global modification test----------------\n"
Str34:	.asciiz	"\tBefore modification:\n"
Str35:	.asciiz	"\tAfter modification:\n"
Str36:	.asciiz	"PASS\n"
Str37:	.asciiz	"----------------9.String test----------------\n"
Str38:	.asciiz	"\tThis string has a \"double quotation\" in it\n"
Str39:	.asciiz	"PASS\n"
Str40:	.asciiz	"----------------10.exit test----------------\n"
Str41:	.asciiz	"If you see nothing, then exit is success\n"
Str42:	.asciiz	"PASS\n"
Str43:	.asciiz	"Wrong man!\n"

```

