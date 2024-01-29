public class Wall {
    Position p1;
    Position p2;

    public Wall(Position p1, Position p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

//    //check if the line created (pos1, pos2) intersect with the wall (p1, p2)
//    public boolean checkIntersect(Position pos1, Position pos2){
//
//        // Calculate the slopes
//        double m1 = (pos2.y - pos1.y) / (pos2.x - pos1.x);
//        double m2 = (p2.y - p1.y) / (p2.x - p1.x);
//
//        // Calculate the y-intercepts
//        double b1 = pos1.y - m1 * pos1.x;
//        double b2 = p1.y - m2 * p1.x;
//
//        // Check for parallel lines (no intersection)
//        if (m1 == m2) {
//            return false;
//        }
//
//        // Calculate the intersection point
//        double intersectionX = (b2 - b1) / (m1 - m2);
//        double intersectionY = m1 * intersectionX + b1;
//
//        // Check if the intersection point is within the bounds of both line segments
//        return isBetween(intersectionX, pos1.x, pos2.x) && isBetween(intersectionY, pos1.y, pos2.y)
//                && isBetween(intersectionX, p1.x, p2.x) && isBetween(intersectionY, p1.y, p2.y);
//
//    }
//
//    //checks if val is collinear between lower and upper
//    private boolean isBetween(double val, double lower, double upper) {
//        return val >= Math.min(lower, upper) && val <= Math.max(lower, upper);
//    }

    /*
        SOURCE: Geeks for Geeks (n.d.). How to check if two given line segments intersect?
        Retrieved from: https://www.geeksforgeeks.org/check-if-two-given-line-segments-intersect/
     */

    public int orientation(Position p, Position q, Position r){
        double val = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y);

        if (val == 0) return 0;  // collinear
        return (val > 0) ? 1 : 2; // clock or counterclock wise
    }

    static boolean onSegment(Position p, Position q, Position r) {
        return q.x <= Math.max(p.x, r.x) && q.x >= Math.min(p.x, r.x) &&
                q.y <= Math.max(p.y, r.y) && q.y >= Math.min(p.y, r.y);
    }
    public boolean doIntersect(Position p1, Position q1, Position p2, Position q2){

        int o1 = orientation(p1, q1, p2);
        int o2 = orientation(p1, q1, q2);
        int o3 = orientation(p2, q2, p1);
        int o4 = orientation(p2, q2, q1);

        // General case
        if (o1 != o2 && o3 != o4)
            return true;
        // Special Cases
        // p1, q1 and p2 are collinear and p2 lies on segment p1q1
        if (o1 == 0 && onSegment(p1, p2, q1)) return true;

        // p1, q1 and q2 are collinear and q2 lies on segment p1q1
        if (o2 == 0 && onSegment(p1, q2, q1)) return true;

        // p2, q2 and p1 are collinear and p1 lies on segment p2q2
        if (o3 == 0 && onSegment(p2, p1, q2)) return true;

        // p2, q2 and q1 are collinear and q1 lies on segment p2q2
        if (o4 == 0 && onSegment(p2, q1, q2)) return true;

        return false; // Doesn't fall in any of the above cases

    }
    public Position getPerpendicularVector(){
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;

        // Rotate 90 deg
        double normalX = -dy;
        double normalY = dx;

        double length = Math.sqrt(normalX * normalX + dx * dx);

        // Normalize the vector
        if (length != 0) {
            normalX /= length;
            normalY /= length;
        }
        return new Position(normalX, normalY);
    }

}
