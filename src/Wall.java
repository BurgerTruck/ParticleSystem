public class Wall {
    Position p1;
    Position p2;
    Position normalVector;
    public Wall(Position p1, Position p2) {
        this.p1 = p1;
        this.p2 = p2;

        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;

        // Rotate 90 deg
        double normalX = -dy;
        double normalY = dx;

        double length = Math.sqrt(normalX * normalX + dx * dx);

        if (length != 0) {
            normalX /= length;
            normalY /= length;
        }
        normalVector= new Position(normalX, normalY);
    }

    public boolean checkIntersection(Position p1, Position q1, Position p2, Position q2){
        double x1 = p1.x, y1 = p1.y;
        double x2 = q1.x, y2 = q1.y;
        double x3 = p2.x, y3 = p2.y;
        double x4 = q2.x, y4 = q2.y;

        double denominator = (x4 - x3) * (y2 - y1) - (y4 - y3) * (x2 - x1);

        if(denominator == 0){
            return false;
        }

        double alpha = ((x4 - x3) * (y3 - y1) - (y4 - y3) * (x3 - x1)) / denominator;
        double beta = ((x2 - x1) * (y3 - y1) - (y2 - y1) * (x3 - x1)) / denominator;

        if(alpha == 0 && denominator == 0){
            return true;
        }

        if(alpha < 1 && alpha > 0 && beta < 1 && beta > 0){
            return true;
        }
        else{
            return false;
        }
    }
    public Position getPerpendicularVector(){

        return normalVector;
    }

}
