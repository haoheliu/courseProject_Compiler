import java.util.Scanner;

/**用于存储词法分析中遇到的标识符
 * */

public class TokenMgr implements Constants
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
                //In order to support the use of element within an array
            } while (Character.isLetterOrDigit(currentChar) || currentChar == '[' || currentChar == ']');
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
            if (token.image.equals("int"))
                token.kind = INT;
            else
            if (token.image.equals("return"))
                token.kind = RETURN;
            else
            if (token.image.equals("def"))
                token.kind = DEF;
            else
            if (token.image.equals("void"))
                token.kind = VOID;
            else
            if (token.image.equals("cal"))
                token.kind = CAL;
            else
            if (token.image.equals("and"))
                token.kind = AND;
            else
            if(token.image.equals("else"))
                token.kind = ELSE;
            else
            if (token.image.equals("or"))
                token.kind = OR;
            else
            if (token.image.equals("switch"))
                token.kind = SWITCH;
            else
            if (token.image.equals("case"))
                token.kind = CASE;
            else
            if (token.image.equals("goto"))
                token.kind = GOTO;
            else
            if (token.image.equals("break"))
                token.kind = BREAK;
            else
            if (token.image.equals("continue"))
                token.kind = CONTINUE;
            else
            if(token.image.equals("dest"))
                token.kind = DEST;
            else
            if(token.image.equals("array"))
                token.kind = ARRAY;
            else
            if(token.image.equals("const"))
                token.kind = CONST;
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
                case ',':
                    token.kind = COMMA;
                    token.image = Character.toString(currentChar);
                    break;
                case '~':
                    token.kind = END;
                    token.image = Character.toString(currentChar);
                case ':':
                    token.kind = COLON;
                    token.image = Character.toString(currentChar);
                    break;
                case '[':
                    token.kind = LEFTBRACKET;
                    token.image = Character.toString(currentChar);
                    break;
                case ']':
                    token.kind = RIGHTBRACKET;
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