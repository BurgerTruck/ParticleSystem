import java.io.Serializable;

public class Input implements Serializable {
    public boolean w;
    public boolean a;
    public boolean s;
    public boolean d;

    public Input(boolean w, boolean a, boolean s, boolean d) {
        this.w = w;
        this.a = a;
        this.s = s;
        this.d = d;
    }

    @Override
    public String toString() {
        return "Input{" +
                "w=" + w +
                ", a=" + a +
                ", s=" + s +
                ", d=" + d +
                '}';
    }
}
