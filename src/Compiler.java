/**
 * @Copyright
 * Author: Haohe Liu from NWPU
 * Time: April.2019
 * */

import com.sun.org.apache.bcel.internal.generic.RET;
import com.sun.org.apache.regexp.internal.RE;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.io.*;
import java.util.*;


public class Compiler
{
    public static void main(String[] args) throws IOException
    {
        if (args.length != 1)
        {
            //命令行需要有待编译的文件名
            System.err.println("Wrong number cmd line args");
            System.exit(1);
        }
        boolean debug = false;
        System.out.println("Directory: "+System.getProperty("user.dir"));
        //输入为一个.c--
        String inFileName = args[0];
        //输出为一个.a文件，可以为我们的assembler使用
        String outFileName = args[0] + ".output";
        //文件读取
        Scanner inFile = new Scanner(new File(inFileName));
        //文件输出
        PrintWriter outFile = new PrintWriter(outFileName);
        //符号表
        SymTab st = new SymTab();
        //词法分析器
        TokenMgr tm =  new TokenMgr(inFile);
        //语法分析器
        Parser parser = new Parser(st, tm, outFile);

        try
        {
            parser.parse();
        }
        //编译错误
        catch (RuntimeException e)
        {
            System.err.println(e.getMessage());
            outFile.println(e.getMessage());
            outFile.close();
            System.exit(1);
        }
        outFile.close();
    }
}


/**这个接口定义了各种我们可能使用到的标识符类型
 * 后续词法分析器和语法分析器等都是对这个接口的实现
 * */
interface Constants
{
    // integers that identify token kinds
    int EOF = 0;
    int PRINTLN = 1;
    int UNSIGNED = 2;
    int ID = 3;
    int ASSIGN = 4;
    int SEMICOLON = 5;
    int LEFTPAREN = 6;
    int RIGHTPAREN = 7;
    int PLUS = 8;
    int MINUS = 9;
    int TIMES = 10;
    int ERROR = 11;
    int DIVIDE = 12;
    int LEFTBRACE = 13;
    int RIGHTBRACE = 14;
    int STRING = 15;
    //switch expression
    int WHILE = 16;
    int IF = 17;
    int ELSE = 18;
    //boolean expression
    int EQUAL = 19;
    int GREATER_THAN = 20;
    int SMALLER_THAN = 21;
    int GREATER_EQUAL_THAN = 22;
    int SMALLER_EQUAL_THAN = 23;
    int INT = 24;   //Used as variable type
    int RETURN = 25;
    int DEF = 26;
    int VOID = 27;
    int CAL = 28;
    int AND = 29;
    int OR = 30;
    int COMMA = 31;
    int END = 32;
    int SWITCH = 33;
    int CASE = 34;
    int DEFAULT = 35;
    int COLON = 36;
    int GOTO = 37;
    int BREAK = 38;
    int CONTINUE = 39;
    int DEST = 40;
    int ARRAY = 41; //Used as variable type
    int LEFTBRACKET = 42;
    int RIGHTBRACKET = 43;

    int CONST = 44; //Used as variable type
    int ARGS = 45;  //Used as variable type

    int EXIT = 46; //Used as the force quit of the hole program
    int ASSERT = 47;

    // tokenImage provides string for each token kind
    String[] tokenImage =
            {
                    "<EOF>",
                    "\"println\"",
                    "<UNSIGNED>",
                    "<ID>",
                    "\"=\"",
                    "\";\"",
                    "\"(\"",
                    "\")\"",
                    "\"+\"",
                    "\"-\"",
                    "\"*\"",
                    "<ERROR>",
                    "\"/\"",
                    "\"{\"",
                    "\"}\"",
                    "<STRING>",
                    "\"while\"",
                    "\"if\"",
                    "\"else\"",
                    "\"==\"",
                    "\">\"",
                    "\"<\"",
                    "\">=\"",
                    "\"<=\"",
                    "int",
                    "return",
                    "def",
                    "void",
                    "cal",
                    "and",
                    "or",
                    ",",
                    "~",//FORCE END
                    "switch",
                    "case",
                    "default",
                    ":",
                    "goto",
                    "break",
                    "continue",
                    "dest",
                    "array",
                    "[",
                    "]"
            };
}



