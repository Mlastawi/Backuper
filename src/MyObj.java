import java.io.Serializable;

/**
 * Created by Maciek on 06/11/2016.
 */
public class MyObj implements Serializable {
    public int a;
    public int b;

    MyObj(int a, int b){
        this.a = a;
        this.b = b;
    }
}
