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
        Position next = getNextPosition(timePassed);
        Position temp = p;
//        boolean collided = false;

        boolean collided = false;
        for (Wall wall : walls) {
            if (wall.checkIntersection(temp, next, wall.p1, wall.p2)) {
                Position wallNormal = wall.getPerpendicularVector();
                double dotProduct = dx * wallNormal.x + dy * wallNormal.y;

                dx -= 2 * dotProduct * wallNormal.x;
                dy -= 2 * dotProduct * wallNormal.y;

                next = new Position(p.x + dx * timePassed, p.y + dy * timePassed);
                collided = true;
            }
        }



        if (next.x < 0 || next.x > GUI.canvasWidth) {
            dx = -dx;
            collided = true;
        }
        if (next.y < 0 || next.y > GUI.canvasHeight) {
            dy = -dy;
            collided = true;
        }


        if(!collided){
            p = new Position(p.x + dx * timePassed, p.y + dy * timePassed);
            prev = p;
        }


//        System.out.println("Next pos: " + next);
//        System.out.println("Wall count: " + walls.size());
    }
}
