import java.util.ArrayList;

public class FuncSymTab
{
    public String func_name;
    public int args_num;
    public int vars_num;
    public int reg_saved;
    private int current_offset;
    private ArrayList<String> args;
    private ArrayList<String> vars;
    private ArrayList<String> arr_names;    //The arraies' name defined within the function
    private ArrayList<Integer> arr_offsets; // The arraies offset (compared with $sp)

    public FuncSymTab(String func_name)
    {
        this.args = new ArrayList<>();
        this.vars = new ArrayList<>();
        this.arr_names = new ArrayList<>();
        this.arr_offsets = new ArrayList<>();
        this.arr_offsets.add(0);

        this.args_num = 0;
        this.vars_num = 0;
        this.current_offset = 0;
        this.func_name = func_name;
    }

    public int getSpace() {
        return 4 * (2 + this.args_num + this.vars_num);
    }

    public void arrEnter(String name,int space)
    {
        //First we need to get rid of the "[xxx]" part of it
        int first_left_bracket = name.indexOf('[');
        name = name.substring(0, first_left_bracket);
        //Then consider whether it is in our array table
        int index = arr_names.indexOf(name);
        if(index<0)
        {
            this.current_offset += space;
            arr_names.add(name);
            arr_offsets.add(current_offset);
        }else genDf(name);
    }

    //Return the offset from the &sp pointer to the start of the array
    public int arrLocate(String name)
    {
        int index = arr_names.indexOf(name);
        if(index >= 0)
        {
            return this.arr_offsets.get(index);
        }else
            throw new RuntimeException("Error: This array have not been defined!");
    }

    //on parsing: enter the args
    public void argEnter(String arg)
    {
        int index = args.indexOf(arg);
        if(index<0)
        {
            args.add(arg);
            args_num++;
        }
        else genDf(arg);
    }

    public void varEnter(String var)
    {
        if(args.indexOf(var)>=0)genDf(var);
        int index = vars.indexOf(var);
        if(index<0)
        {
            vars.add(var);
            vars_num++;
        }
        else genDf(var);
    }

    public int varLength()
    {
        return this.vars.size();
    }
    public int argLength()
    {
        return this.args.size();
    }
    //locate the memory location of this arg
    public int argLocate(String arg)
    {
        return args.indexOf(arg);
    }

    public int varLocate(String arg)
    {
        return vars.indexOf(arg);
    }
    public void reset()
    {
        this.func_name = "";
        this.args.clear();
        this.vars.clear();
        this.vars_num = 0;
        this.args_num = 0;
    }
    //Exceptions
    private void genDf(String item)
    {
        throw new RuntimeException("\nError: "+item+" is already defined");
    }
    private void genNf(String item)
    {
        throw new RuntimeException("\nError: "+item+" not defined");
    }
}