import java.util.ArrayList;

public class Particle {
    Position p;
    Position prev;
    double speed; //pixels/s
    double angle;




//    private long lastMoved = -1;
    private double dx;
    private double dy;

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

    public void move(ArrayList<Wall> walls, double timePassed){
//        long time = System.nanoTime();
//        if(lastMoved==-1) lastMoved = time;
//        double elapsed = (time - lastMoved)/1000000000d;
//        System.out.println(elapsed);
//        lastMoved = time;
        Position next = getNextPosition(timePassed)  ;
        //TODO: check collision
        if(next.x < 0 || next.x > GUI.canvasWidth){
            dx = -dx;
        }
        if(next.y < 0 || next.y > GUI.canvasHeight){
            dy = -dy;
        }

        Position temp = p;
        p = next;
        System.out.println("Next pos: " + next);

        prev = temp;

    }
}
