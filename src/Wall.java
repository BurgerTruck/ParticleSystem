public class Wall {
    Position p1;
    Position p2;

    public Wall(Position p1, Position p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    //check if the line created (pos1, pos2) intersect with the wall (p1, p2)
    public boolean checkIntersect(Position pos1, Position pos2){

        // Calculate the slopes
        double m1 = (pos2.y - pos1.y) / (pos2.x - pos1.x);
        double m2 = (p2.y - p1.y) / (p2.x - p1.x);

        // Calculate the y-intercepts
        double b1 = pos1.y - m1 * pos1.x;
        double b2 = p1.y - m2 * p1.x;

        // Check for parallel lines (no intersection)
        if (m1 == m2) {
            return false;
        }

        // Calculate the intersection point
        double intersectionX = (b2 - b1) / (m1 - m2);
        double intersectionY = m1 * intersectionX + b1;

        // Check if the intersection point is within the bounds of both line segments
        return isBetween(intersectionX, pos1.x, pos2.x) && isBetween(intersectionY, pos1.y, pos2.y)
                && isBetween(intersectionX, p1.x, p2.x) && isBetween(intersectionY, p1.y, p2.y);

    }

    private boolean isBetween(double val, double lower, double upper) {
        return val >= Math.min(lower, upper) && val <= Math.max(lower, upper);
    }
    public Position getPerpendicularVector(){
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;

        // Rotate 90 deg
        double normalX = -dy;

        double length = Math.sqrt(normalX * normalX + dx * dx);
        return new Position(normalX / length, dx / length);
    }

}
