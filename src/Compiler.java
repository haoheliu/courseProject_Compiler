/**
 * @Copyright
 * Author: Haohe Liu from NWPU
 * Time: April.2019
 * */

import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.io.*;
import java.util.*;

/**
 * About the compiler:
 *  function:
 *      compiler the C-like language to MIPS instructions
 *  Error  report:
 *      Inside the switch-case block, throw error encountered
 *
 * The grammer we support now:
 *  Variable definations:
 *      All variable is considered as int type.Once we use a identifier, it will be considered defined
 *  Assignment statement:
 *      Assign a value(int,string,expression) to an idenetifier, if use a identifier before initialization, the variable will be
 *      automatically set 0
 *  Calculation:
 *      We support :
 *          Ops: +,-,*,/
 *          Order change: ()
 *  Select block:
 *      if-else:
 *          if(exp1){}
 *          else(exp2){}
 *      while:
 *          while(exp1){}
 *      Note: the boole expression is not realized, the criteria is whether expression is zero or not
 *  Comment:
 *      use double backslash to add comment:
 *          example: //comments
 *  Other built-in functions:
 *      println():
 *          print the number to output device
 *          print the string to output device
 *              the String can contain quotes, for example: "Compiler \\\" construction"
 * */

class Compiler
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
        String outFileName = args[0] + ".a";
        //文件读取
        Scanner inFile = new Scanner(new File(inFileName));
        //文件输出
        PrintWriter outFile = new PrintWriter(outFileName);
        //符号表
        SymTab st = new SymTab();
        //词法分析器
        TokenMgr tm =  new TokenMgr(inFile);
        //代码生成器
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
    int WHILE = 16;
    int IF = 17;
    int ELSE = 18;

    int EQUAL = 19;
    int GREATER_THAN = 20;
    int SMALLER_THAN = 21;
    int GREATER_EQUAL_THAN = 22;
    int SMALLER_EQUAL_THAN = 23;

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
            };
}



/**对于一个token,我们保留它的起始位置和终止位置
 * 同时保存它的类型和image(就是这个东西在.c--文件里边本身长什么样子)
 * 也存储下一个token的引用
 * */
class Token implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public int kind;
    //token开始的行
    public int beginLine;
    //token开始的列
    public int beginColumn;
    //token结束的行
    public int endLine;
    //token结束的列
    public int endColumn;
    //token的字符串镜像
    public String image;
    //token的值,变量的定义
    //对下一个token的引用,相当于C语言里边的next指针
    public Token next;

    //构造函数
    public Token() {}
    public Token(int kind)
    {
        this(kind, null);
    }
    public Token(int kind, String image)
    {
        this.kind = kind;
        this.image = image;
    }
    public String toString()
    {
        return image;
    }
}

/**用于存储词法分析中遇到的标识符
 * */
class SymTab
{
    private ArrayList<String> symbol;
    //ArrayList: add & indexOf
    public SymTab()
    {
        symbol = new ArrayList<>();
    }
    //查询是否在符号表中，如果不在则加入
    public void enter(String s)
    {
        int index = symbol.indexOf(s);
        if (index < 0)
            symbol.add(s);
    }
    //指定index的项目
    public String getSymbol(int index)
    {
        return symbol.get(index);
    }
    //查看当前符号表有几个项目
    public int getSize()
    {
        return symbol.size();
    }
}

class TokenMgr implements Constants
{
    private Scanner inFile;
    private char currentChar;
    private int currentColumnNumber;
    private int currentLineNumber;
    private String inputLine;     // holds 1 line of input
    private Token token;          // holds 1 token
    private StringBuffer buffer;  // token image built here
    private boolean inString;
    //-----------------------------------------
    public TokenMgr(Scanner inFile)
    {
        this.inFile = inFile;
        currentChar = '\n';        //  '\n' triggers read
        currentLineNumber = 0;
        buffer = new StringBuffer();
        inString = false;
    }

    //-----------------------------------------
    public Token getNextToken()
    {
        // skip whitespace
        while (Character.isWhitespace(currentChar))
            getNextChar();

        token = new Token();
        token.next = null;
        token.beginLine = currentLineNumber;
        token.beginColumn = currentColumnNumber;

        // check for EOF
        if (currentChar == EOF)
        {
            token.image = "<EOF>";
            token.endLine = currentLineNumber;
            token.endColumn = currentColumnNumber;
            token.kind = EOF;
        }
        else
            if (Character.isDigit(currentChar))
            {
                buffer.setLength(0);
                do
                {
                    buffer.append(currentChar);
                    token.endLine = currentLineNumber;
                    token.endColumn = currentColumnNumber;
                    getNextChar();
                } while (Character.isDigit(currentChar));
                token.image = buffer.toString();
                token.kind = UNSIGNED;
            }

            else
                if (Character.isLetter(currentChar))
                {
                    buffer.setLength(0);
                    do
                    {
                        buffer.append(currentChar);
                        token.endLine = currentLineNumber;
                        token.endColumn = currentColumnNumber;
                        getNextChar();
                    } while (Character.isLetterOrDigit(currentChar));
                    token.image = buffer.toString();

                    if (token.image.equals("println"))
                        token.kind = PRINTLN;
                    else
                    if (token.image.equals("while"))
                        token.kind = WHILE;
                    else
                    if (token.image.equals("if"))
                        token.kind = IF;
                    else
                    if (token.image.equals("else"))
                        token.kind = ELSE;
                    else  // not a keyword so kind is ID
                        token.kind = ID;
                }
                else if (currentChar == '"') {
                    boolean done = false;
                    inString = true;
                    int backslashCounter = 0;
                    buffer.setLength(0);  // clear buffer
                    while (!done) {
                        do  // build token image in buffer
                        {
                            if (currentChar == '\\'){
                                backslashCounter++;
                            }
                            buffer.append(currentChar);
                            getNextChar();
                            if (currentChar != '\\' && currentChar != '"'){
                                backslashCounter = 0;
                            }
                            try {
                                if (currentChar == '\\' && inputLine.charAt(currentColumnNumber+1) == '\n'){
                                    getNextChar();
                                }
                            } catch (Exception e) {
                                getNextChar();
                            }
                            if (currentChar == '\n' || currentChar == '\r') {
                                break;
                            }
                        } while (currentChar != '"');
                        if (currentChar =='"' && backslashCounter % 2 == 0) //quote precede with even number of backslash
                        {
                            done = true;
                            backslashCounter = 0;
                            buffer.append(currentChar);
                            token.kind = STRING;
                        }
                        else if (currentChar =='"' && backslashCounter % 2 != 0) {
                            backslashCounter = 0;
                            continue;
                        }
                        else
                            token.kind = ERROR;
                        token.endLine = currentLineNumber;
                        token.endColumn = currentColumnNumber;
                        getNextChar();
                        token.image = buffer.toString();
                        inString = false;
                    }
                }
                else  // process single-character token
                {
                    switch(currentChar)
                    {
                        case '=':
                            if(lookAhead(1) == '=')
                            {
                                token.kind = EQUAL;
                                getNextChar();
                                token.image = "==";
                            } else {
                                token.image = Character.toString(currentChar);
                                token.kind = ASSIGN;
                            }
                            break;
                        case '>':
                            if(lookAhead(1) == '=')
                            {
                                token.kind = GREATER_EQUAL_THAN;
                                getNextChar();
                                token.image = ">=";
                            }else {
                                token.image = Character.toString(currentChar);
                                token.kind = GREATER_THAN;
                            }
                            break;
                        case '<':
                            if(lookAhead(1) == '=')
                            {
                                token.kind = SMALLER_EQUAL_THAN;
                                getNextChar();
                                token.image = "<=";
                            }else
                            {
                                token.image = Character.toString(currentChar);
                                token.kind = SMALLER_THAN;
                            }
                            break;
                        case ';':
                            token.kind = SEMICOLON;
                            token.image = Character.toString(currentChar);
                            break;
                        case '(':
                            token.kind = LEFTPAREN;
                            token.image = Character.toString(currentChar);
                            break;
                        case ')':
                            token.kind = RIGHTPAREN;
                            token.image = Character.toString(currentChar);
                            break;
                        case '+':
                            token.kind = PLUS;
                            token.image = Character.toString(currentChar);
                            break;
                        case '-':
                            token.kind = MINUS;
                            token.image = Character.toString(currentChar);
                            break;
                        case '*':
                            token.kind = TIMES;
                            token.image = Character.toString(currentChar);
                            break;
                        case '/':
                            token.kind = DIVIDE;
                            token.image = Character.toString(currentChar);
                            break;
                        case '{':
                            token.kind = LEFTBRACE;
                            token.image = Character.toString(currentChar);
                            break;
                        case '}':
                            token.kind = RIGHTBRACE;
                            token.image = Character.toString(currentChar);
                            break;
                        default:
                            token.kind = ERROR;
                            token.image = Character.toString(currentChar);
                            break;
                    }

                    // save currentChar as String in token.image

                    // save token end location
                    token.endLine = currentLineNumber;
                    token.endColumn = currentColumnNumber;
                    getNextChar();  // read beyond end
                }

        return token;
    }
    private char lookAhead(int amount)
    {
        try
        {
            char next = inputLine.charAt(currentColumnNumber+amount-1);
            return next;
        }
        catch (Exception e){System.out.println("Error");}
        return ' ';
    }
    //-----------------------------------------
    private void getNextChar()
    {
        if (currentChar == EOF)
            return;

        if (currentChar == '\n')
        {
            if (inFile.hasNextLine())     // any lines left?
            {
                inputLine = inFile.nextLine();  // get next line
                inputLine = inputLine + "\n";   // mark line end
                currentLineNumber++;
                currentColumnNumber = 0;
            }
            else  // at EOF
            {
                currentChar = EOF;
                return;
            }
        }
        // check if single-line comment
        if (!inString &&
                inputLine.charAt(currentColumnNumber) == '/' &&
                inputLine.charAt(currentColumnNumber+1) == '/')
            currentChar = '\n';  // forces end of line
        else
            currentChar = inputLine.charAt(currentColumnNumber++);
    }
}

/**
 * Bounding each .ascii2 string with a identifier -> MIPS
 * */
class StringMgr
{
    private ArrayList<String> collection;
    private int stringcount;

    public StringMgr()
    {
        this.stringcount = 0;
        this.collection = new ArrayList<>();
    }
    private String stringIdAvailable()
    {
        return "Str"+this.stringcount++;
    }
    public String enter(String str)
    {
        String str_id = stringIdAvailable();
        collection.add(str);
        return str_id;
    }
    public int getSize()
    {
        return collection.size();
    }
    public String getItem(int index)
    {
        return collection.get(index);
    }
}

class Parser implements Constants
{
    private SymTab st;
    private TokenMgr tm;
    private PrintWriter outFile;
    private Token currentToken;
    private Token previousToken;
    private int identifiercount;
    private int registercount;
    private int registerS_count;
    private StringMgr sm;
    private ArrayList<String> StringIdentifiers;
    public Parser(SymTab st, TokenMgr tm, PrintWriter outFile)
    {
        this.st = st;
        this.tm = tm;
        this.outFile = outFile;
        this.identifiercount = 0;
        this.registercount = 0;
        this.sm = new StringMgr();
        this.StringIdentifiers = new ArrayList<>();

        currentToken = tm.getNextToken();
        previousToken = null;
    }
    /**
     * registerAvailable: return a free register
     * resetRegister: clear and reset the use of registers
     * identifierAvailable: Generate a label for jump instruction
     * */

    private String registerAvailable()
    {
        String temp = "$t"+this.registercount++;
        //Totally we have $t0~$t9, so if we don't have enough register, we will throw an exception
        if(this.registercount == 11)
        {
            throw genEx("Temporary registor overflow");
        }
        return temp;
    }
    private void resetRegister()
    {
        this.registerS_count = 0;
        this.registercount = 0;
    }
    private String identifierAvailable(){ return "L"+this.identifiercount++;}
    private String registerS_Available(){
        String temp = "$s"+this.registerS_count++;
        if(this.registerS_count == 9)
        {
            throw genEx("S registor overflow");
        }
        return temp;
    }



    private RuntimeException genEx(String errorMessage)
    {
        return new RuntimeException("Encountered \"" +
                currentToken.image + "\" on line " +
                currentToken.beginLine + ", column " +
                currentToken.beginColumn + "." +
                errorMessage);
    }

    private void advance()
    {
        previousToken = currentToken;
        if (currentToken.next != null)
            currentToken = currentToken.next;
        else
            currentToken = currentToken.next = tm.getNextToken();
    }
    private void consume(int expected)
    {
        if (currentToken.kind == expected)
            advance();
        else
            throw genEx("Expecting " + tokenImage[expected]);
    }

    public void parse()
    {
        program();
    }

    private void program()
    {
        outFile.println("\t.text");
        statementList();
        if (currentToken.kind != EOF)
            throw genEx("Expecting <EOF>");
        dataSegment();
    }

    private void statementList()
    {
        switch(currentToken.kind)
        {
            case ID:
            case WHILE:
            case PRINTLN:
            case IF:
                statement();
                statementList();
                break;
            case EOF:
            case RIGHTBRACE:
                break;
            default:
                throw genEx("Expecting statement or <EOF>");
        }
    }
    private void statement()
    {
        switch(currentToken.kind)
        {
            case ID:
                assignmentStatement();
                break;
            case PRINTLN:
                printlnStatement();
                break;
            case WHILE:
                whileStatement();
                break;
            case LEFTBRACE:
                compoundStatement();
                break;
            case IF:
                ifStatement();
                break;
            default:
                throw genEx("Expecting statement");
        }
    }

    private void assignmentStatement()
    {

        Token t = currentToken;
        String left_op = t.image; //identifier on the left
        consume(ID);
        String reg = registerAvailable();
        outFile.println("lw\t"+reg+"\t"+t.image);
        st.enter(t.image);
        consume(ASSIGN);
        String temp = expr();
        //Strx are not allowed to use
        try{
            if(StringIdentifiers.contains(left_op)) //Every time before we use, we first delete it
            {
                this.StringIdentifiers.remove(left_op);
            }
            if(temp.substring(0, 3).equals("Str") && !StringIdentifiers.contains(left_op))
            {
                this.StringIdentifiers.add(left_op);
            }
        }catch (Exception e){}

        String reg_temp = isNeedRegister(temp);
        outFile.println("move\t"+reg+"\t"+reg_temp);
        outFile.println("sw\t"+reg+"\t"+t.image);
//        outFile.println("mov"+"\t"+temp+",\t"+t.image);
        resetRegister();
        System.out.println(temp);
        consume(SEMICOLON);
    }

    private void printlnStatement()
    {
        String temp;
        consume(PRINTLN);
        consume(LEFTPAREN);
        outFile.println("#println Statement");
        temp = expr();
        try
        {
            if(temp.substring(0, 3).equals("Str"))
            {
                String reg_temp = isNeedRegister(temp);
                outFile.println("li\t"+"$v0,\t"+"4");
                outFile.println("move\t"+"$a0\t"+reg_temp);
                outFile.println("syscall");
            }
            consume(RIGHTPAREN);
            consume(SEMICOLON);
            return;
        }
        catch (Exception e){}

        String reg_temp = isNeedRegister(temp);

        if(this.StringIdentifiers.contains(temp))
        {
            outFile.println("li\t"+"$v0,\t"+"4");
        }
        else
        {
            outFile.println("li\t"+"$v0,\t"+"1");
        }

        outFile.println("move\t"+"$a0\t"+reg_temp);
        outFile.println("syscall");
        consume(RIGHTPAREN);
        consume(SEMICOLON);
    }

    private void compoundStatement()
    {
        consume(LEFTBRACE);
        statementList();
        consume(RIGHTBRACE);
    }

    private void whileStatement()
    {
        String judge_point = identifierAvailable();
        String judge_exit = identifierAvailable();
        String judge;
        outFile.println(judge_point+":");
        consume(WHILE);
        consume(LEFTPAREN);
        judge = expr();

        String reg_judge = isNeedRegister(judge);

        outFile.println("beq"+"\t$zero"+",\t"+reg_judge+",\t"+judge_exit);
        consume(RIGHTPAREN);
        statement();
        outFile.println("j"+"\t"+judge_point);
        outFile.println(judge_exit+":");
        System.out.println("Successfully parse while");
    }

    //-----------------------------------------
    private void ifStatement() {
        String judge_else = identifierAvailable();
        String judge_exit = identifierAvailable();
        consume(IF);
        consume(LEFTPAREN);
        String judge = expr();
        String reg_judge = isNeedRegister(judge);
        outFile.println("beq"+"\t$zero"+",\t"+reg_judge+",\t"+judge_else);
        consume(RIGHTPAREN);
        statement();
        outFile.println("j"+"\t"+judge_exit);
        elsePart(judge_else);
        outFile.println(judge_exit+":");
    }
    //-----------------------------------------
    private void elsePart(String judge_else) {
        outFile.println(judge_else+":");
        switch (currentToken.kind) {
            case ELSE:
                consume(ELSE);
                statement();
                break;
            default:
        }
    }
    private String expr()
    {
        String term_val,expr_val,termlist_syn;
        term_val = term();
        termlist_syn = termList(term_val);
        expr_val = termlist_syn;
        return expr_val;
    }
    /**
     *Allocate register for data in memory or immediate data
     * */
    private String isNeedRegister(String term)
    {
        System.out.println(term);
        try
        {
            //If it's string immediate, use la
            if(term.substring(0,3).equals("Str"))
            {
                String reg = registerAvailable();
                outFile.println("la"+"\t"+reg+"\t"+term);
                return reg;
            }
        }
        catch (Exception e){}
        if(term.charAt(0) == '$')
        {
            return term;
        }else if(Character.isDigit(term.charAt(0)))
        {
            String reg = registerAvailable();
            outFile.println("li"+"\t"+reg+"\t"+term);
            return reg;
        }
        else
        {
            String reg = registerAvailable();
            outFile.println("lw"+"\t"+reg+"\t"+term);
            return reg;
        }
    }
    private String termList(String inh)
    {
        String term_val,termlist_syn;
        String reg_inh;
        String reg_term_val;
        String reg_plus;
        String reg_result,reg_equ;
        switch(currentToken.kind)
        {
            case PLUS:
                consume(PLUS);
                term_val = term();

                reg_inh = isNeedRegister(inh);
                reg_term_val = isNeedRegister(term_val);
                reg_result = registerAvailable();

                outFile.println("add"+"\t"+reg_result +",\t" + reg_term_val +",\t"+reg_inh);

                termlist_syn = termList(reg_result);
                break;
            case MINUS:
                consume(MINUS);
                term_val = term();

                reg_inh = isNeedRegister(inh);
                reg_term_val = isNeedRegister(term_val);
                reg_result = registerAvailable();

                outFile.println("sub"+"\t"+reg_result +",\t" + reg_inh +",\t"+reg_term_val);

                termlist_syn = termList(reg_result);
                break;
            case EQUAL:
                consume(EQUAL);
                termlist_syn = expr();

                reg_inh = isNeedRegister(inh);
                reg_term_val = isNeedRegister(termlist_syn);
                reg_result = registerS_Available();
                reg_equ = registerS_Available();

                //Next determine whether they are equal
                outFile.println("xor\t"+reg_equ+",\t"+reg_term_val+",\t"+reg_inh);
                outFile.println("slti\t"+reg_result+",\t"+reg_equ+",\t1");

                termlist_syn = reg_result;

                resetRegister();
                System.out.println("EQUAL, Diter: "+inh+" "+termlist_syn);

                break;
            case GREATER_EQUAL_THAN:
                consume(GREATER_EQUAL_THAN);
                termlist_syn = expr();

                reg_inh = isNeedRegister(inh);
                reg_term_val = isNeedRegister(termlist_syn);
                reg_result = registerS_Available();
                reg_equ = registerS_Available();

                //First determine greater or not
                outFile.println("slt\t"+reg_result+",\t"+reg_term_val+",\t"+reg_inh);
                //Next determine whether they are equal
                outFile.println("xor\t"+reg_equ+",\t"+reg_term_val+",\t"+reg_inh);
                outFile.println("slti\t"+reg_equ+",\t"+reg_equ+",\t1");
                //both greater and equal can set the reg_result to 1
                outFile.println("or\t"+reg_result+",\t"+reg_equ+",\t"+reg_result);

                termlist_syn = reg_result;
                resetRegister();
                System.out.println("GREATER_EQUAL_THAN, compare: "+reg_inh+" "+reg_term_val);
                break;
            case SMALLER_EQUAL_THAN:
                consume(SMALLER_EQUAL_THAN);
                termlist_syn = expr();

                reg_inh = isNeedRegister(inh);
                reg_term_val = isNeedRegister(termlist_syn);
                reg_result = registerS_Available();
                reg_equ = registerS_Available();

                //First determine greater or not
                outFile.println("slt\t"+reg_result+",\t"+reg_inh+",\t"+reg_term_val);
                //Next determine whether they are equal
                outFile.println("xor\t"+reg_equ+",\t"+reg_term_val+",\t"+reg_inh);
                outFile.println("slti\t"+reg_equ+",\t"+reg_equ+",\t1");
                //both greater and equal can set the reg_result to 1
                outFile.println("or\t"+reg_result+",\t"+reg_equ+",\t"+reg_result);

                termlist_syn = reg_result;
                resetRegister();

                System.out.println("SMALLER_EQUAL_THAN, compare: "+inh+" "+termlist_syn);
                break;
                
            case GREATER_THAN:
                consume(GREATER_THAN);
                termlist_syn = expr();

                reg_inh = isNeedRegister(inh);
                reg_term_val = isNeedRegister(termlist_syn);
                reg_result = registerS_Available();

                //First determine greater or not
                outFile.println("slt\t"+reg_result+",\t"+reg_term_val+",\t"+reg_inh);

                termlist_syn = reg_result;
                resetRegister();

                System.out.println("GREATER_THAN, compare: "+inh+" "+termlist_syn);
                break;

            case SMALLER_THAN:
                consume(SMALLER_THAN);
                termlist_syn = expr();

                reg_inh = isNeedRegister(inh);
                reg_term_val = isNeedRegister(termlist_syn);
                reg_result = registerS_Available();

                //First determine greater or not
                outFile.println("slt\t"+reg_result+",\t"+reg_inh+",\t"+reg_term_val);
                termlist_syn = reg_result;
                resetRegister();
                System.out.println("SMALLER_THAN, compare: "+inh+" "+termlist_syn);
                break;
            case RIGHTPAREN:
            case SEMICOLON:
                termlist_syn = inh;
                break;
            default:
                throw genEx("Expecting \"+\", \")\", or \";\"");
        }
        return termlist_syn;
    }

    private String term()
    {
        String factorlist_inh,term_val,factorlist_syn;
        factorlist_inh = factor();
        factorlist_syn = factorList(factorlist_inh);
        term_val = factorlist_syn;
        return term_val;
    }

    private String factorList(String inh)
    {
        String factor_val,factorlist_syn;
        switch(currentToken.kind)
        {
            case TIMES:
                consume(TIMES);
                factor_val = factor();

                String reg_inh = isNeedRegister(inh);
                String reg_factor_val = isNeedRegister(factor_val);

                String newidentifier = registerAvailable();
                outFile.println("mult"+"\t"+reg_inh+",\t"+reg_factor_val);
                outFile.println("mflo\t"+newidentifier);

                factorlist_syn = factorList(newidentifier);
                break;
            case DIVIDE:
                consume(DIVIDE);
                factor_val = factor();

                String reg_inh_div = isNeedRegister(inh);
                String reg_factor_val_div = isNeedRegister(factor_val);
                String newidentifier_div = registerAvailable();

                outFile.println("div"+"\t"+reg_inh_div+",\t"+reg_factor_val_div);
                outFile.println("mflo\t"+newidentifier_div);

                factorlist_syn = factorList(newidentifier_div);
                break;
            case PLUS:
            case MINUS:
            case RIGHTPAREN:
            case SEMICOLON:
            case EQUAL:
            case GREATER_EQUAL_THAN:
            case SMALLER_EQUAL_THAN:
            case GREATER_THAN:
            case SMALLER_THAN:
                factorlist_syn = inh;
                break;
            default:
                throw genEx("Expecting op, \")\", or \";\"");
        }
        return factorlist_syn;
    }

    private String factor()
    {
        Token t;
        String factor_val;
        switch(currentToken.kind)
        {
            case UNSIGNED:
                t = currentToken;
                consume(UNSIGNED);
                factor_val = t.image;
                break;
            case PLUS:
                consume(PLUS);
                t = currentToken;
                consume(UNSIGNED);
                factor_val = t.image;
                break;
            case MINUS:
                consume(MINUS);
                t = currentToken;
                consume(UNSIGNED);
                factor_val = "-"+t.image;
                break;
            case ID:
                t = currentToken;
                consume(ID);
                st.enter(t.image);
                factor_val = t.image;
                break;
            case STRING:
                t = currentToken;
                consume(STRING);
                factor_val = sm.enter(t.image); //Automatic another line
                break;
            case LEFTPAREN:
                consume(LEFTPAREN);
                factor_val = expr();
                consume(RIGHTPAREN);
                break;
            default:
                throw genEx("Expecting factor");
        }
        return factor_val;
    }

    /**
     * Generate ".data" segment in MIPS instructions
     * Based on symbol table we have
     * */
    private void dataSegment()
    {
        outFile.println("\t"+".data");
        for(int i=0;i<sm.getSize();i++)
        {
            String str = sm.getItem(i);
            outFile.println("Str"+i+":\t"+".asciiz\t"+str);
        }
        for (int i = 0;i<st.getSize();i++)
        {
            String temp = st.getSymbol(i);
            outFile.println(temp+":\t"+".word\t"+"0");
        }
    }

}

