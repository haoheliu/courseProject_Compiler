import java.util.Scanner;

/**对于一个token,我们保留它的起始位置和终止位置
 * 同时保存它的类型和image(就是这个东西在.c--文件里边本身长什么样子)
 * 也存储下一个token的引用
 * */
public class Token implements java.io.Serializable {
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

