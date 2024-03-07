import java.util.ArrayList;

public class World {
    private ArrayList<Particle> particles;
    private ArrayList<Wall> walls;

    public World(){
        particles = new ArrayList<>();
        walls = new ArrayList<>();
    }

    public ArrayList<Particle> getParticles() {
        return particles;
    }

    public ArrayList<Wall> getWalls() {
        return walls;
    }



}
