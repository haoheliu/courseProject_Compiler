import java.util.ArrayList;

/**
 * Bounding each .ascii2 string with a identifier -> MIPS
 * */
public class StringMgr
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
