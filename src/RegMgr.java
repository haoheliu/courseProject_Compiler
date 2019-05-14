public class RegMgr
{
    public int registerT_count;
    public int registerS_count;
    public int registerA_count;


    public RegMgr()
    {
        this.registerT_count = 0;
        this.registerS_count = 0;
        this.registerA_count = 0;
    }
    public String registerAvailable()
    {
        String temp = "$t"+this.registerT_count++;
        //Totally we have $t0~$t9, so if we don't have enough register, we will throw an exception
        if(this.registerT_count == 11)
        {
            throw new RuntimeException("Temporary registor overflow");
        }
        return temp;
    }

    public String registerS_Available(){
        String temp = "$s"+this.registerS_count++;
        if(this.registerS_count == 9)
        {
            throw new RuntimeException("s registor overflow");
        }
        return temp;
    }

    public String registerA_Available(){
        String temp = "$a"+this.registerA_count++;
        if(this.registerA_count == 5)
        {
            throw new RuntimeException("Registor a overflow");
        }
        return temp;
    }

    /**
     * registerAvailable: return a free register
     * resetRegister: clear and reset the use of registers
     * identifierAvailable: Generate a label for jump instruction
     * */

    public void resetRegister()
    {
        this.registerS_count = 0;
        this.registerT_count = 0;
        this.registerA_count = 0;
    }

}