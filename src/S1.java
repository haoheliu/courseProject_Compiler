import java.io.*;
import java.util.*;


class S1
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
        System.out.println(System.getProperty("user.dir"));
        //输入为一个.c--
        String inFileName = args[0];
        //输出为一个.a文件，可以为我们的assembler使用
        String outFileName = args[0] + ".a";
        //文件读取
        Scanner inFile = new Scanner(new File(inFileName));
        //文件输出
        PrintWriter outFile = new PrintWriter(outFileName);
        //符号表
        S1SymTab st = new S1SymTab();
        //词法分析器
        S1TokenMgr tm =  new S1TokenMgr(inFile);
        //代码生成器
        //语法分析器
        S1Parser parser = new S1Parser(st, tm, outFile);

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
interface S1Constants
{

    int EOF = 0;        //文档结束
    int PRINTLN = 1;    //println
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
                    "<ERROR>"
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
class S1SymTab
{
    private ArrayList<String> symbol;
    //ArrayList: add & indexOf
    public S1SymTab()
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


class S1TokenMgr implements S1Constants
{
    private Scanner inFile;
    private char currentChar;
    private int currentColumnNumber;
    private int currentLineNumber;
    private String inputLine;
    private Token token;
    private StringBuffer buffer;

    public S1TokenMgr(Scanner inFile)
    {
        this.inFile = inFile;
        currentChar = '\n';
        currentLineNumber = 0;
        buffer = new StringBuffer();
    }

    /**
     * 获得下一个token对象
     * */
    public Token getNextToken()
    {
        //跳过空白符
        while (Character.isWhitespace(currentChar))
            getNextChar();
        //实例化一个token对象
        token = new Token();
        //下一个token的引用
        token.next = null;

        token.beginLine = currentLineNumber;
        token.beginColumn = currentColumnNumber;

        //判断是否读取完毕，读取到文件的<EOF>
        if (currentChar == EOF)
        {
            token.image = "<EOF>";
            token.endLine = currentLineNumber;
            token.endColumn = currentColumnNumber;
            token.kind = EOF;
        }
        //没有读取结束
        else
            /*__________整数部分的读取__________*/
            if (Character.isDigit(currentChar))
            {
                //清空缓冲区
                buffer.setLength(0);
                //开始读取整数
                do
                {
                    buffer.append(currentChar);
                    token.endLine = currentLineNumber;
                    token.endColumn = currentColumnNumber;
                    getNextChar();
                } while (Character.isDigit(currentChar));
                token.image = buffer.toString();//image就是这个整数本身转化为字符串
                token.kind = UNSIGNED;
            }

            else
                /*__________标识符的读取__________*/
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

                    if (token.image.equals("println")) //判断是否是关键字
                        token.kind = PRINTLN;
                    else
                        token.kind = ID;
                }

                else
                {
                    /*__________单个字符的读取__________*/
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
                        default:
                            token.kind = ERROR;
                            break;
                    }

                    token.image = Character.toString(currentChar);
                    token.endLine = currentLineNumber;
                    token.endColumn = currentColumnNumber;

                    getNextChar();
                }

        return token;
    }

    private void getNextChar()
    {
        // 删掉了EOF判断,这里不需要
        if (currentChar == '\n')
        {
            if (inFile.hasNextLine())
            {
                inputLine = inFile.nextLine();
                inputLine = inputLine + "\n";
                //从头读取一行
                currentColumnNumber = 0;
                currentLineNumber++;
            }
            else
            {
                //读到没有行就认为是结束了
                currentChar = EOF;
                return;
            }
        }
        currentChar = inputLine.charAt(currentColumnNumber++);
    }
}

class S1Parser implements S1Constants
{
    private S1SymTab st;
    private S1TokenMgr tm;
    private PrintWriter outFile;
    private Token currentToken;
    private Token previousToken;
    private int identifiercount;
    private Map<String,String> identifier;

    public S1Parser(S1SymTab st, S1TokenMgr tm, PrintWriter outFile)
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
        return "L"+this.identifiercount++;
    }

    private RuntimeException genEx(String errorMessage)
    {
        return new RuntimeException("Encountered \"" +
                currentToken.image + "\" on line " +
                currentToken.beginLine + ", column " +
                currentToken.beginColumn + "." +
                System.getProperty("line.separator") +
                errorMessage);
    }

    private void advance()
    {
        previousToken = currentToken;
        if (currentToken.next != null)
            currentToken = currentToken.next;
        else
            currentToken =
                    currentToken.next = tm.getNextToken();
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
        statementList();
        if (currentToken.kind != EOF)
            throw genEx("Expecting <EOF>");
    }

    private void statementList()
    {
        switch(currentToken.kind)
        {
            case ID:
            case PRINTLN:
                statement();
                statementList();
                break;
            case EOF:
                ;
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
        System.out.println(temp);
        consume(SEMICOLON);
    }
    private void printlnStatement()
    {
        consume(PRINTLN);
        consume(LEFTPAREN);
        expr();
        consume(RIGHTPAREN);
        consume(SEMICOLON);
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
                outFile.println("add"+"\t"+inh +"\t" + term_val +"\t"+newidentifier);
                identifier.put(newidentifier,termlist_inh);
                termlist_syn = termList(newidentifier);
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
                outFile.println("mult"+"\t"+inh+"\t"+factor_val+"\t"+newidentifier);
                identifier.put(newidentifier, factorlist_inh);
                factorlist_syn = factorList(newidentifier);
                break;
            case PLUS:
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
}

