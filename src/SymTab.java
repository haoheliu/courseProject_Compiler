import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SymTab
{
    private ArrayList<String> symbol;
    private Map<String,FuncSymTab> func_tabs;
    private ArrayList<String> global_var;   // Storage for global variables
    private int current_offset;             //Store how much space we have used for arraies;
    private ArrayList<String> global_arr_name;
    private ArrayList<Integer> global_arr_offset;

    //ArrayList: add & indexOf
    public SymTab()
    {
        symbol = new ArrayList<>();
        func_tabs = new HashMap<>();
        global_var = new ArrayList<>();
        global_arr_name =  new ArrayList<>();
        global_arr_offset = new ArrayList<>();
        global_arr_offset.add(0); //The first element in the global_arr_name's offset should be 0
        this.current_offset = 0;
    }

    public int getGlobalVarSize()
    {
        return global_var.size()*4;
    }

    public void addGlobalArr(String name,int space)
    {
        //First we need to get rid of the "[xxx]" part of it
        int first_left_bracket = name.indexOf('[');
        name = name.substring(0, first_left_bracket);
        //Then consider whether it is in our array table
        int index = global_arr_name.indexOf(name);
        if(index<0)
        {
            this.current_offset += space;
            global_arr_name.add(name);
            global_arr_offset.add(current_offset);
        }else throw new RuntimeException("Error: global variable"+name+" have already been defined");
    }

    public int locateGlobalArr(String name)
    {
        int index = global_arr_name.indexOf(name);
        if(index >= 0)
        {
            return this.global_arr_offset.get(index);
        }else
            return -1;
    }

    public void addGlobal(String s)
    {
        if(global_var.contains(s))
            throw new RuntimeException("Error: "+s+" has already been defined!");
        global_var.add(s);
    }

    public int locateGlobal(String s)
    {
        int index = global_var.indexOf(s);
        if(index < 0)
            index = -1;
        return index;
    }

    public void enter(String s)
    {
        int index = symbol.indexOf(s);
        if (index < 0)
            symbol.add(s);
    }

    public void enterFunc(String func_name,FuncSymTab func)
    {
        if (!func_tabs.containsKey(func_name))
            func_tabs.put(func_name, func);
        else throw new RuntimeException("Error: Function \""+func_name+"\" has already defined");
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
