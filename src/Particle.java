import java.io.Serializable;
import java.util.ArrayList;

public class Particle implements Serializable {
    Position p;
    transient Position prev;

    transient double speed; //pixels/s
    transient double angle;

    private transient double dx;
    private transient double dy;

    public Particle(Position p, double speed, double a) {
        this.p = p;
        this.speed = speed;

        angle = Math.toRadians(a);
        dx = Math.cos(angle) * speed;
        dy = Math.sin(angle) * speed;
        prev = new Position(p.x, p.y);

    }

    public Position getNextPosition(double timePassed){
        return new Position(p.x+dx*timePassed, p.y+dy*timePassed);
    }

    public void move( double timePassed){
        Position next = getNextPosition(timePassed);
        boolean collided = false;

        if (next.x < 0 || next.x >= GUI.canvasWidth) {
            dx = -dx;
            collided = true;
        }
        if (next.y < 0 || next.y >= GUI.canvasHeight) {
            dy = -dy;
            collided = true;
        }

        if(!collided){
            p = next;
            prev = p;
        }

    }




    @Override
    public String toString() {
        return "Particle{" +
                "p=" + p +
                '}';
    }
    public static int numBytes(){
        return Position.numBytes();
    }
    public byte[] toBytes(){
        return p.toBytes();
    }
    public static Particle decodeBytes(byte[] bytes     ){
        return new Particle(Position.decodeBytes(bytes), 0, 0   );
    }
}
