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
        boolean collided = true;

//        while (true) {
//            boolean collided = false;
//
//            for (Wall wall : walls) {
//
//                if (wall.hasIntersect(temp, next)) {
//                    collided = true;
//                    Position wallNormal = wall.getPerpendicularVector();
//                    double dotProduct = dx * wallNormal.x + dy * wallNormal.y;
//
//                    // Reflect the direction
//                    dx -= 2 * dotProduct * wallNormal.x;
//                    dy -= 2 * dotProduct * wallNormal.y;
//
//                    // Update the next position based on the adjusted direction
//                    next = new Position(p.x + dx * timePassed, p.y + dy * timePassed);
//                }
//            }
//
//            // If no collisions occurred, exit the loop
//            if (!collided) {
//                break;
//            }
//        }


        for(Wall wall : walls) {
            if(wall.checkIntersect(temp, next)) {

                Position wallNormal = wall.getPerpendicularVector();
                double dotProduct = dx * wallNormal.x + dy * wallNormal.y;

                // Reflect the direction
                dx -= 2 * dotProduct * wallNormal.x;
                dy -= 2 * dotProduct * wallNormal.y;


                next = new Position(p.x + dx * timePassed, p.y + dy * timePassed);
            }
        }



        // Reflect direction if hitting canvas borders
        if (next.x < 0 || next.x > GUI.canvasWidth) {
            dx = -dx;
        }
        if (next.y < 0 || next.y > GUI.canvasHeight) {
            dy = -dy;
        }

        // Update position if no collision occurred
        p = new Position(p.x + dx * timePassed, p.y + dy * timePassed);
        prev = p;

        System.out.println("Next pos: " + next);
        System.out.println("Wall count: " + walls.size());
    }
}
