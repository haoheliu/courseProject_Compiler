import java.io.*;
import java.util.*;


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
    int PRINT = 15;
    int READINT = 16;
    int STRING = 17;
    int WHILE = 18;
    int IF = 19;
    int ELSE = 20;
    int DO = 21;

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
                    "\"print\"",
                    "\"readint\"",
                    "<STRING>",
                    "\"while\"",
                    "\"if\"",
                    "\"else\"",
                    "\"do\""
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
        symbol = new ArrayList<String>();
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
                    if (token.image.equals("readint"))
                        token.kind = READINT;
                    else
                    if (token.image.equals("while"))
                        token.kind = WHILE;
                    else
                    if (token.image.equals("if"))
                        token.kind = IF;
                    else
                    if (token.image.equals("do"))
                        token.kind = DO;
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
                        if (currentChar =='"' && backslashCounter % 2 == 0)
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
                            token.kind = ASSIGN;
                            break;
                        case ';':
                            token.kind = SEMICOLON;
                            break;
                        case '(':
                            token.kind = LEFTPAREN;
                            break;
                        case ')':
                            token.kind = RIGHTPAREN;
                            break;
                        case '+':
                            token.kind = PLUS;
                            break;
                        case '-':
                            token.kind = MINUS;
                            break;
                        case '*':
                            token.kind = TIMES;
                            break;
                        case '/':
                            token.kind = DIVIDE;
                            break;
                        case '{':
                            token.kind = LEFTBRACE;
                            break;
                        case '}':
                            token.kind = RIGHTBRACE;
                            break;
                        default:
                            token.kind = ERROR;
                            break;
                    }

                    // save currentChar as String in token.image
                    token.image = Character.toString(currentChar);

                    // save token end location
                    token.endLine = currentLineNumber;
                    token.endColumn = currentColumnNumber;

                    getNextChar();  // read beyond end
                }

        return token;
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
            currentChar =
                    inputLine.charAt(currentColumnNumber++);
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
    private Map<String,String> identifier;

    public Parser(SymTab st, TokenMgr tm, PrintWriter outFile)
    {
        this.st = st;
        this.tm = tm;
        this.outFile = outFile;
        this.identifiercount = 0;
        this.identifier = new HashMap<>();
        currentToken = tm.getNextToken();
        previousToken = null;
    }

    private String identifierGenerate()
    {
        return "$t"+this.identifiercount++;
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
            default:
                throw genEx("Expecting statement");
        }
    }

    private void assignmentStatement()
    {
        Token t;
        t = currentToken;
        consume(ID);
        st.enter(t.image);
        consume(ASSIGN);
        String temp = expr();
        outFile.println("mov"+"\t"+temp+",\t"+t.image);
        System.out.println(temp);
        consume(SEMICOLON);
    }

    private void printlnStatement()
    {
        String temp;
        consume(PRINTLN);
        consume(LEFTPAREN);
        outFile.println(";println Statement");
        temp = expr();
        consume(RIGHTPAREN);
        consume(SEMICOLON);
        outFile.println("print"+"\t\t"+temp);
        outFile.println("print"+"\t\t"+"\n");
    }

    private void compoundStatement()
    {
        consume(LEFTBRACE);
        statementList();
        consume(RIGHTBRACE);
    }

    private void whileStatement()
    {
        String judge_point = identifierGenerate();
        String judge;
        outFile.println(judge_point+":");
        Token t = currentToken;
        consume(WHILE);
        consume(LEFTPAREN);
        judge = expr();
        outFile.println("bne"+"\t0"+",\t"+judge+",\tExit");
        consume(RIGHTPAREN);
        statement();
        outFile.println("j"+"\t"+judge_point);
        outFile.println("Exit:");
        System.out.println("Successfully parse while");
    }

    private String expr()
    {
        String term_val,expr_val,termlist_syn;
        term_val = term();
        termlist_syn = termList(term_val);
        expr_val = termlist_syn;
        return expr_val;
    }

    private String termList(String inh)
    {
        String termlist_inh,term_val,termlist_syn;
        switch(currentToken.kind)
        {
            case PLUS:
                consume(PLUS);
                term_val = term();
                termlist_inh = inh + "+" + term_val;
                String newidentifier = identifierGenerate();
                System.out.println("inh:"+inh+"\tterm_val:"+term_val + "\t+"+" -> "+newidentifier);
                outFile.println("add"+"\t"+inh +",\t" + term_val +",\t"+newidentifier);
                identifier.put(newidentifier,termlist_inh);
                termlist_syn = termList(newidentifier);
                break;
            case MINUS:
                consume(MINUS);
                term_val = term();
                termlist_inh = inh + "+" +term_val;
                String newidentifier_minus = identifierGenerate();
                System.out.println("inh:"+inh+"\tterm_val:"+term_val + "\t-"+" -> "+newidentifier_minus);
                outFile.println("sub"+"\t"+inh +",\t" + term_val +",\t"+newidentifier_minus);
                identifier.put(newidentifier_minus,termlist_inh);
                termlist_syn = termList(newidentifier_minus);
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
        String factorlist_inh,factor_val,factorlist_syn;
        switch(currentToken.kind)
        {
            case TIMES:
                consume(TIMES);
                factor_val = factor();
                factorlist_inh = inh + "*" +factor_val;
                String newidentifier = identifierGenerate();
                System.out.println("inh:"+inh+"\tfactor_val:"+factor_val + "\t*"+" -> "+newidentifier);
                outFile.println("mult"+"\t"+inh+",\t"+factor_val+",\t"+newidentifier);
                identifier.put(newidentifier, factorlist_inh);
                factorlist_syn = factorList(newidentifier);
                break;
            case DIVIDE:
                consume(DIVIDE);
                factor_val = factor();
                factorlist_inh = inh + "/" +factor_val;
                String newidentifier_div = identifierGenerate();
                System.out.println("inh:"+inh+"\tfactor_val:"+factor_val + "\t/"+" -> "+newidentifier_div);
                outFile.println("div"+"\t"+inh+",\t"+factor_val+",\t"+newidentifier_div);
                identifier.put(newidentifier_div, factorlist_inh);
                factorlist_syn = factorList(newidentifier_div);
                break;
            case PLUS:
            case MINUS:
            case RIGHTPAREN:
            case SEMICOLON:
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
    private void dataSegment()
    {
        outFile.println("\t"+".data");
        for(int i=0;i<st.getSize();i++)
        {
            String symbol = st.getSymbol(i);
            outFile.println(symbol+":\t"+".word\t"+"0");
        }
    }
}

