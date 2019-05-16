import java.util.ArrayList;


class FuncSymTab implements Constants
{
    String name;
    public int base_offset;
    public ArrayList<Var> vars;

    public int local_var_num;
    public int local_args_num;

    public int space;

    public FuncSymTab(){}

    public FuncSymTab(String name)
    {
        this.name = name;
        vars= new ArrayList<>();
        this.base_offset = 0;
        this.local_var_num = 0;
        this.local_args_num = 0;
    }
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
            }else{
                throw new RuntimeException("Error: ["+name+"] Variable has already been defined");
            }
        }else throw new RuntimeException("Error: The function \"Enter\" used is not compatible");
    }
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
    /**
     * @LastStepOfFuncSymTab
     * The initial value in "Var" is their size
     * After this function we will have it's offset from base_pointer
     * */
    public void initCalBasementValue()
    {
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

    public void reset()
    {
        this.vars= new ArrayList<>();
        this.base_offset = 0;
        this.local_var_num = 0;
        this.local_args_num = 0;
    }

}

class Var implements Constants
{
    public String name;
    public int offset; //
    public int size;
    public int type;

    public Var(){}

    //For array
    public Var(String name,int type,int size)
    {
        if(type != ARRAY)
            throw new RuntimeException("Error: Wrong construction method");
        this.name = name;
        this.type = type;
        this.size = size;
    }
    //For int and constant
    public Var(String name,int type)
    {
        if(type != CONST && type != INT && type != ARGS)
            throw new RuntimeException("Error: Wrong construction method");
        this.name = name;
        this.type = type;
        this.size = 4;
    }

    @Override
    public boolean equals(Object o)
    {
        if(o instanceof Var)
        {
            if( ((Var) o).name.equals(this.name) &&
                    ((Var) o).type == this.type)
                return true;
            else return false;
        }
        return false;
    }
}
