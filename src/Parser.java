import java.io.PrintWriter;
import java.util.ArrayList;

public class Parser implements Constants
{
    private SymTab st;
    private TokenMgr tm;
    private PrintWriter outFile;
    private Token currentToken;
    private Token previousToken;
    private int identifiercount;
    private RegMgr rm;
    private StringMgr sm;

    private ArrayList<String> StringIdentifiers;

    private String exitpoint; //The label we need for break statement
    private String judgepoint; //The label we need for continue statement

    int array_space = 0;

    private FuncSymTab ft;
    //the function we are working in
    private String currentfunction;
    public Parser(SymTab st, TokenMgr tm, PrintWriter outFile)
    {
        this.st = st;
        this.tm = tm;
        this.outFile = outFile;
        this.identifiercount = 0;
        this.rm = new RegMgr();
        this.sm = new StringMgr();

        this.array_space = 0;

        this.exitpoint = "";
        this.judgepoint = "";

        this.StringIdentifiers = new ArrayList<>();

        this.currentfunction = "main";
        //This instance is shared between different functions, after parsing one, it will be reset
        this.ft = new FuncSymTab("main");


        currentToken = tm.getNextToken();
        previousToken = null;
    }

    private void emitInstruction(String func,String op1)
    {
        outFile.println(func+"\t"+op1);
    }
    private void emitInstruction(String func,String op1,String op2)
    {
        outFile.println(func+"\t"+op1+",\t"+op2);
    }
    private void emitInstruction(String func,String op1,String op2,String op3)
    {
        outFile.println(func+"\t"+op1+",\t"+op2+",\t"+op3);
    }


    private String identifierAvailable(){ return "L"+this.identifiercount++;}

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

    private void programUnitList()
    {
        switch (currentToken.kind)
        {
            case DEF:
            case INT:
            case ARRAY:
                programUnit();
                programUnitList();
                break;
            default:
                break;
        }
    }

    private void programUnit()
    {
        switch (currentToken.kind)
        {
            case DEF:
                functionDefinition();
                break;
            case INT:
            case ARRAY:
                globalDeclarations();
                break;
            default:
                break;
        }
    }

    private void functionDefinition()
    {
        consume(DEF);
        consume(VOID);
        //Entrance of a function
        outFile.println("\n"+currentToken.image+":");
        //Update the function parsing in
        currentfunction = currentToken.image;
        ft.func_name = currentfunction;
        consume(ID);

        consume(LEFTPAREN);
        int space_para = parameterList(); //empty
        consume(RIGHTPAREN);
        consume(LEFTBRACE);
        int space_local = localDeclarations();
        statementList();
        returnStatement();

        emitInstruction("lw", "$ra","4($fp)");
        emitInstruction("lw", "$fp","0($fp)");
        emitInstruction("addi", "$sp","$sp",""+ft.getSpace());
        emitInstruction("jr", "$ra");

        consume(RIGHTBRACE);

        st.enterFunc(currentfunction, ft);
        //Reset function
        ft.reset();

        this.array_space = 0; //Reset the record for array space use within a function calling stack
    }

    private int parameterList()
    {
        int space;
        switch (currentToken.kind)
        {
            case INT:
                parameter();
                parameterTail();
                space = -4*(ft.argLength()+2);
                emitInstruction("addi", "$sp","$sp",space+"");
                for(int i=0;i<ft.args_num;i++)
                {
                    emitInstruction("sw","$a"+i,(4*(ft.argLength()+1-i))+"($sp)");
                }
                emitInstruction("sw", "$ra","4($sp)");
                emitInstruction("sw", "$fp","0($sp)");
                emitInstruction("move", "$fp","$sp");
                return space;
            default:
                space = -8;
                emitInstruction("addi", "$sp","$sp",space+"");
                emitInstruction("sw", "$ra","4($sp)");
                emitInstruction("sw", "$fp","0($sp)");
                emitInstruction("move", "$fp","$sp");
                return -8;
        }
    }

    //Some global declarations between function definations
    private void globalDeclarations()
    {
        switch (currentToken.kind)
        {
            case INT:
                consume(INT);
                String nameGloble = currentToken.image;
                st.addGlobal(nameGloble);// Add this global variable into the symbol table
                emitInstruction("addi","$gp","$gp","4"); // Spare some space for global variabless
                consume(ID);
                globalTail();
                consume(SEMICOLON);
                globalDeclarations();
                break;
            case ARRAY:
                consume(ARRAY);
                String id = currentToken.image;
                int space = Integer.parseInt(id.substring(id.indexOf('[')+1, id.indexOf(']')))*4;

                st.addGlobalArr(currentToken.image, space);

                consume(ID);
                consume(SEMICOLON);
                globalDeclarations();
                break;
            default:
                break;
        }
    }

    private void globalTail()
    {
        switch (currentToken.kind)
        {
            case COMMA:
                consume(COMMA);
                String nameGloble = currentToken.image;
                st.addGlobal(nameGloble);
                emitInstruction("addi","$gp","$gp","4"); // Spare some space for global variabless
                consume(ID);

            default:
                break;
        }
    }

    private int localDeclarations()
    {
        int space = 0;
        switch (currentToken.kind)
        {
            case INT:
                consume(INT);
                ft.varEnter(currentToken.image);
                consume(ID);
                localTail();
                space = (-4)*ft.varLength();
                //Expand memory space according to the declaration
                emitInstruction("addi", "$sp","$sp",""+space);
                consume(SEMICOLON);
                //Recursively call this function and sum the overall space together!
                space += localDeclarations();
                return space;
            case ARRAY:
                consume(ARRAY);
                //Process ID: "id[xxx]"
                String id = currentToken.image;
                space = Integer.parseInt(id.substring(id.indexOf('[')+1, id.indexOf(']')))*4;
                ft.arrEnter(currentToken.image,space); //Enter this array into the symbol table

                consume(ID);
                consume(SEMICOLON);

                emitInstruction("addi", "$sp","$sp","-"+space); //!!!!!!!!!!!!!!!!!!!!!!!!
                System.out.println("Successfully parse array!");
                space += localDeclarations();
                return space;
            default:
                return 0;
        }
    }

    private void localTail()
    {
        switch (currentToken.kind)
        {
            case COMMA:
                consume(COMMA);
                ft.varEnter(currentToken.image);
                consume(ID);
                localTail();
            default:
                break;
        }
    }

    private void parameter()
    {
        consume(INT);
        //enter the args into the symtable of this function
        ft.argEnter(currentToken.image);
        consume(ID);
    }

    private void parameterTail()
    {
        switch (currentToken.kind)
        {
            case COMMA:
                consume(COMMA);
                parameter();
                parameterTail();
                break;
            default:
                break;
        }
    }

    private void program()
    {
        outFile.println("\t.text");
        emitInstruction("move", "$fp","$sp");
        emitInstruction("jal", "main");
        emitInstruction("j", "exit");
        programUnitList();
        if (currentToken.kind != EOF)
            throw genEx("Expecting <EOF>");
        outFile.println("exit:");
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
            case GOTO:
            case BREAK:
            case CONTINUE:
            case DEST:
            case CAL:
            case RETURN:
            case LEFTBRACE:
            case SWITCH:
                statement();
                statementList();
                break;
            case EOF:
            case RIGHTBRACE:
            case CASE:
            case DEFAULT:
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
            case RETURN:
                returnStatement();
                break;
            case CAL:
                functionCall();
                break;
            case SWITCH:
                switchStatement();
                break;
            case GOTO:
            case BREAK:
            case CONTINUE:
            case DEST:
                jumpStatement();
                break;
            default:
                throw genEx("Expecting statement");
        }
    }

    private void jumpStatement()
    {
        switch (currentToken.kind)
        {
            case GOTO:
                consume(GOTO);
                String destination = currentToken.image;
                consume(ID);
                consume(SEMICOLON);
                emitInstruction("j", destination);
                break;
            case BREAK:
                consume(BREAK);
                consume(SEMICOLON);
                emitInstruction("j", exitpoint);
                break;
            case CONTINUE:
                consume(CONTINUE);
                consume(SEMICOLON);
                emitInstruction("j", judgepoint);
                break;
            case DEST:
                consume(DEST);
                String place = currentToken.image;
                consume(ID);
                consume(SEMICOLON);
                outFile.println(place+":");
                break;
            default:
                break;
        }
    }

    private void switchStatement()
    {
        consume(SWITCH);
        consume(LEFTPAREN);
        expr();
        consume(RIGHTPAREN);
        consume(LEFTBRACE);
        caseStatementList();
        defaultStatement();
        consume(RIGHTBRACE);
        System.out.println("!!!!!!!!!!!!");
    }

    private void caseStatementList()
    {
        switch(currentToken.kind)
        {
            case CASE:
                caseStatement();
                caseStatementList();
            default:
                break;
        }
    }

    private void caseStatement()
    {
        switch (currentToken.kind)
        {
            case CASE:
                consume(CASE);
                String image = currentToken.image;
                consume(UNSIGNED);
                consume(COLON);
                statementList();
            default:
                break;
        }
    }

    private void defaultStatement()
    {
        switch(currentToken.kind)
        {
            case DEFAULT:
                consume(DEFAULT);
                consume(COLON);
                statementList();
            default:
                break;
        }
    }

    private boolean returnStatement()
    {
        switch (currentToken.kind)
        {
            case RETURN:
                consume(RETURN);
                String reg_result = isNeedRegister(expr());
                emitInstruction("move", "$v0",reg_result);
                consume(SEMICOLON);

                return true;
            default:
                return false;
        }
    }

    private void functionCall()
    {
        consume(CAL);
        String func_name = currentToken.image;
        consume(ID);
        consume(LEFTPAREN);

        argumentList();

        consume(RIGHTPAREN);
        //consume(SEMICOLON);

        //argumentList will also consume registers
        int reg_t = rm.registerT_count;
        int reg_s = rm.registerS_count;
        int save_reg = (-4)*(reg_s+reg_t);

        System.out.println("reg"+rm.registerT_count);
        System.out.println("reg"+rm.registerS_count);

        emitInstruction("addi", "$sp","$sp",""+save_reg);

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

        rm.resetRegister();

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
        emitInstruction("addi", "$sp","$sp",""+(-1)*save_reg);
    }
    /**
     * @ArgumentList
     * */
    private void argumentList()
    {
        switch (currentToken.kind)
        {
            case RIGHTPAREN:
                break;
            default:
                String reg = rm.registerA_Available();
                String reg_result = isNeedRegister(expr());
                emitInstruction("move", reg,reg_result);
                argtail();
                break;
        }
    }

    private void argtail()
    {
        switch (currentToken.kind)
        {
            case COMMA:
                consume(COMMA);
                String reg = rm.registerA_Available();
                String reg_result = isNeedRegister(expr());
                emitInstruction("move", reg,reg_result);
                argtail();
                break;
            default:
                break;
        }
    }

    /**
     * This function is defined in order to unify the "load" operation from local variables and global varibales
     * */
    private void loadVariable(String reg,String var)
    {
        int index;
        if(var.indexOf('[') > 0 && var.indexOf(']') > 0)
        {
            /***************
             * First consider whether this variable is an array
             * Then we need some code to loadvariable into register
             * ********/
            String name = var.substring(0, var.indexOf('['));
            int arr_index = Integer.parseInt(var.substring(var.indexOf('[')+1, var.indexOf(']')));

            int offset = st.locateGlobalArr(name);
            if(offset < 0)
            {
                offset = ft.arrLocate(name);
                emitInstruction("lw", reg,offset+arr_index*4+"($sp)");
            }else
            {
                int base = st.getGlobalVarSize(); //Start from the last item of global variables
                emitInstruction("lw",reg,base+offset+arr_index*4+"($gp)");
            }

//            System.out.println("String name:"+name);
//            System.out.println("String name:"+offset);
        }else {
            //Firstly we find these variables in global variable list
            index = st.locateGlobal(var);
            System.out.println("index of the globle variable: "+index);
            if(index >= 0) // If this variable is found in global variable list
            {
                emitInstruction("lw",reg,(index*4)+"($gp)");
                return;
            }
            //Search this symbol in symtable
            index = ft.argLocate(var);
            if(index >= 0)
            {
                //If this variable is defined in args list
                /**@marked
                 * The push sequence and index sequence are inverse
                 * So we need some tricks
                 * */
                emitInstruction("lw",reg, (4*(ft.argLength()+1)-index*4)+"($fp)");
            }
            else if(index < 0)
            {
                //If this variable is defined in local variables list
                index = ft.varLocate(var);
                if(index >= 0)emitInstruction("lw",reg, this.array_space+(4*(ft.varLength()-1)-index*4)+"($sp)");

            }
            if(index < 0) throw genEx(var+" not defined");
        }
    }

    private void saveVariable(String reg,String var)
    {
        int index;
        if(var.indexOf('[') > 0 && var.indexOf(']') > 0)
        {
            String name = var.substring(0, var.indexOf('['));
            int arr_index = Integer.parseInt(var.substring(var.indexOf('[')+1, var.indexOf(']')));
            int offset = st.locateGlobalArr(name);

            if(offset < 0)
            {
                offset = ft.arrLocate(name);
                emitInstruction("sw", reg,offset+arr_index*4+"($sp)");
            }else
            {
                int base = st.getGlobalVarSize(); //Start from the last item of global variables
                emitInstruction("sw",reg,base+offset+arr_index*4+"($gp)");
            }


        }else{
            //Firstly we find these variables in global variable list
            index = st.locateGlobal(var);
            System.out.println("index of the globle variable: "+index);
            if(index >= 0) // If this variable is found in global variable list
            {
                emitInstruction("sw", reg,(index*4)+"($gp)");
                return;
            }
            index = ft.argLocate(var);
            //Defined in args list
            if(index >= 0)
            {
                emitInstruction("sw",reg, (4*(ft.argLength()+1)-index*4)+"($fp)");
            }
            else if(index < 0)
            {
                index = ft.varLocate(var);
                emitInstruction("sw",reg, this.array_space+(4*(ft.varLength()-1)-index*4)+"($sp)");
            }
            if(index < 0) throw genEx(var+" not defined");
        }
    }

    private void assignmentStatement()
    {
        Token t = currentToken;
        String left_op = t.image; //identifier on the left
        consume(ID);
        String reg = rm.registerAvailable();

        loadVariable(reg,left_op);

        st.enter(t.image); // Here we need to consider whether it is an array, if so, stop entering into the function table!!!!!!!!!!!!!!!
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
        emitInstruction("move", reg,reg_temp);

        saveVariable(reg,t.image);

        rm.resetRegister();
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
                emitInstruction("li","$v0","4");
                emitInstruction("move", "$a0",reg_temp);
                outFile.println("syscall");
                consume(RIGHTPAREN);
                consume(SEMICOLON);
                return;
            }
        }
        catch (Exception e){
            System.out.println("Exception happen 1");
        }

        String reg_temp = isNeedRegister(temp);

        if(this.StringIdentifiers.contains(temp))
        {
            emitInstruction("li", "$v0","4");
        }
        else
        {
            emitInstruction("li", "$v0","1");
        }

        emitInstruction("move","$a0",reg_temp);
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
        String judge_point = identifierAvailable(); //Start the judge of while statement
        String judge_exit = identifierAvailable(); // The label of the exit point of while statement

        exitpoint = judge_exit; // These two label are used by break and continue statements
        judgepoint = judge_point;

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
                String reg = rm.registerAvailable();
                emitInstruction("la", reg,term);
                return reg;
            }
        }
        catch (Exception e){}
        if(term.charAt(0) == '$')
        {
            return term;
        }else if(Character.isDigit(term.charAt(0)))
        {
            String reg = rm.registerAvailable();
            emitInstruction("li", reg,term);
            return reg;
        }
        else
        {
            String reg = rm.registerAvailable();
            loadVariable(reg, term);
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
                reg_result = rm.registerAvailable();

                emitInstruction("add", reg_result,reg_term_val,reg_inh);

                termlist_syn = termList(reg_result);
                break;

            case MINUS:
                consume(MINUS);
                term_val = term();

                reg_inh = isNeedRegister(inh);
                reg_term_val = isNeedRegister(term_val);
                reg_result = rm.registerAvailable();

                emitInstruction("sub", reg_result,reg_inh,reg_term_val);

                termlist_syn = termList(reg_result);
                break;

            case EQUAL:
                consume(EQUAL);
                termlist_syn = expr();

                reg_inh = isNeedRegister(inh);
                reg_term_val = isNeedRegister(termlist_syn);
                reg_result = rm.registerS_Available();
                reg_equ = rm.registerS_Available();

                emitInstruction("seq", reg_result,reg_inh,reg_term_val);

                termlist_syn = reg_result;

                rm.resetRegister();
                System.out.println("EQUAL, Diter: "+inh+" "+termlist_syn);
                break;

            case GREATER_EQUAL_THAN:
                consume(GREATER_EQUAL_THAN);
                termlist_syn = expr();

                reg_inh = isNeedRegister(inh);
                reg_term_val = isNeedRegister(termlist_syn);
                reg_result = rm.registerS_Available();
                reg_equ = rm.registerS_Available();

                emitInstruction("sge", reg_result,reg_inh,reg_term_val);

                termlist_syn = reg_result;
                rm.resetRegister();

                System.out.println("GREATER_EQUAL_THAN, compare: "+reg_inh+" "+reg_term_val);
                break;

            case SMALLER_EQUAL_THAN:
                consume(SMALLER_EQUAL_THAN);
                termlist_syn = expr();

                reg_inh = isNeedRegister(inh);
                reg_term_val = isNeedRegister(termlist_syn);
                reg_result = rm.registerS_Available();
                reg_equ = rm.registerS_Available();

                emitInstruction("sle", reg_result,reg_inh,reg_term_val);

                termlist_syn = reg_result;
                rm.resetRegister();

                System.out.println("SMALLER_EQUAL_THAN, compare: "+inh+" "+termlist_syn);
                break;

            case GREATER_THAN:
                consume(GREATER_THAN);
                termlist_syn = expr();

                reg_inh = isNeedRegister(inh);
                reg_term_val = isNeedRegister(termlist_syn);
                reg_result = rm.registerS_Available();

                emitInstruction("sgt", reg_result,reg_inh,reg_term_val);

                termlist_syn = reg_result;
                rm.resetRegister();
                System.out.println("GREATER_THAN, compare: "+inh+" "+termlist_syn);
                break;

            case SMALLER_THAN:
                consume(SMALLER_THAN);
                termlist_syn = expr();

                reg_inh = isNeedRegister(inh);
                reg_term_val = isNeedRegister(termlist_syn);
                reg_result = rm.registerS_Available();

                emitInstruction("slt",reg_result, reg_inh,reg_term_val);

                termlist_syn = reg_result;
                rm.resetRegister();
                System.out.println("SMALLER_THAN, compare: "+inh+" "+termlist_syn);
                break;
            case AND:
            case OR:
                booleanExpression();
                termlist_syn = inh;//!!!!!!!!!!!!!!!!wrong


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

    private void booleanExpression()
    {
        switch (currentToken.kind)
        {
            case AND:
                consume(AND);
                expr();
                booleanExpression();
                break;
            case OR:
                consume(OR);
                expr();
                booleanExpression();
                break;
            default:
                break;
        }
    }

    private String term()
    {
        switch(currentToken.kind)
        {
            case CAL:
                functionCall();
                return "$v0";
            default:
                String factorlist_inh,term_val,factorlist_syn;
                factorlist_inh = factor();
                factorlist_syn = factorList(factorlist_inh);
                term_val = factorlist_syn;
                return term_val;
        }

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

                String newidentifier = rm.registerAvailable();
                emitInstruction("mult", reg_inh,reg_factor_val);
                emitInstruction("mflo", newidentifier);

                factorlist_syn = factorList(newidentifier);
                break;
            case DIVIDE:
                consume(DIVIDE);
                factor_val = factor();

                String reg_inh_div = isNeedRegister(inh);
                String reg_factor_val_div = isNeedRegister(factor_val);
                String newidentifier_div = rm.registerAvailable();

                emitInstruction("div", reg_inh_div,reg_factor_val_div);
                emitInstruction("mflo", newidentifier_div);

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
            case AND:
            case OR: //~!!!!!!!!!!!!!!!!!!!!!!!!!wrong
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
                factor_val = rm.registerAvailable();
                loadVariable(factor_val, t.image);
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
            case CAL:
                functionCall();
                factor_val = "$v0";
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
    }
}
