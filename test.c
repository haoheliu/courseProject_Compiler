//SOME GLOBAL VARIABLES
int test1,test2;
array arr1[4];
array arr2[8];

def void modifyGlobal()
{
    test1 = 111;
    test2 = 222;
    arr1[3] = 65535;
    arr2[1] = 100;
}

def int Fabio(int n)    //recursive
{
    array retval[2];
	if(n == 1 or n ==2)
	{
		return 1;
	}
	else
	{
	    retval[0] = cal Fabio(n-1);
	    retval[1] = cal Fabio(n-2);

		return retval[0]+retval[1];
	}
}

def int factor( int N ){
	if( N > 1 ){
		return N * cal factor( N - 1 );
	}
	else{
		return 1;
	}
}

def void main(int argtest,int argtest2)
{
                                                //ARRAY TEST
    array s[100];
    int a,times;              //test whether the offset is set correctly
    int result,start,end;           //For the test of Fibonacci sequence
    int short1,short2,short3;
    array s2[4];
    const int test = 50+8*(66+3);
    //START TEST!!!
    println("******************Compiler naive****************");
    println("*****************author:Haohe Liu***************");
    println("*******************START TEST*******************");
    println("----------------1.Array test----------------");

    s2[1] = 456;
    assert(s2[1], 100);
    a = 543;
    assert(a, 543);
    s[3] = 655*a + s2[1];    //test the load and save of array element
    assert(s[3], 356121);
    argtest = 100;
    assert(argtest, 100);
    a = 1+(2+4)*100;
    assert(a,601);
    println("PASS");
                                                //RECURSIVE TEST
    println("----------------2.recursive test------------");

    println("=========factorial test=========");
    times = 6;
    a = cal factor(times);
    println("\tResult of recursive test:\n\t");
    println(a);
    println("PASS");

    println("=========Fibonacci sequence test=========");
    start = 1;
    end = 10;
    println("\tFibonacci sequence calculated by recursion from 1 to 10");
    while(start <= end)
    {
        result = cal Fabio(start);
        println(result);
        start = start + 1;
    }
    println("PASS");
                                                //WHILE TEST
    println("----------------3.while test----------------");
    println("\tCount down from 10 to 5:");
    times = 10;
    while(times >= 0)
    {
        println(times);
        if(times == 4){
            break;
        }
        times  = times -1;
    }
    println("PASS");
                                                //IF TEST
    println("----------------4.if test----------------");
    println("\tValue of start: \n\t");
    println(start);
    println("\tValue of end \n\t");
    println(end);
    if(start == 1 or end == 10){
        println("\tstart == 1 or end == 10");
    }
    if(start < 1 or end > 10)
    {
        println("\tstart < 1 or end > 10");
    }else
    {
        println("\tstart >= 1 or end <= 10");
    }

    println("PASS");
                                                //GOTO TEST
    println("----------------5.goto test----------------");
    println("\tStart of goto test, you will see nothing if it works");
    goto end;
    println("\tIf you see this, it means you are wrong!");
    dest end;
    println("PASS");
                                                //SHORT CIRCUIT TEST
    println("----------------6.short circuit test----------------");
    short1 = 10;
    short2 = 100;
    short3 = 1000;
    assert(short3,1000);
    while(short1 == 10 and short2 <= 99 and short3 > 900)
    {
        println("\tShort circuit!");
        break;
    }
    println("PASS");
                                                //CONST TEST

    println("----------------7.constant modification test----------------");
    println("!attension: in order to perform this test, please modify the original code");
    //test = 100;  //Const variable cannot be modified
    println("PASS");

    println("----------------8.global modification test----------------");
    println("\tBefore modification:");
    println(test1);
    println(test2);
    println(arr1[3]);
    println(arr2[1]);
    cal modifyGlobal()   //Call this function to modify the global variable
    println("\tAfter modification:");
    println(test1);
    println(test2);
    println(arr1[3]);
    println(arr2[1]);
    println("PASS");

    println("----------------9.String test----------------");
    println("\tThis string has a \"double quotation\" in it");
    println("PASS");


    println("----------------10.exit test----------------");
    println("If you see nothing, then exit is success");
    println("PASS");
    exit;
    println("Wrong man!");


}