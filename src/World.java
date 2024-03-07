import java.util.ArrayList;

public class World {
    private ArrayList<Particle> particles;
    private ArrayList<Wall> walls;
    private Controller controller;
    public World(Controller controller){
        particles = new ArrayList<>();
        walls = new ArrayList<>();
        this.controller = controller;
        controller.setWorld(this);
    }

    public ArrayList<Particle> getParticles() {
        return particles;
    }

    public ArrayList<Wall> getWalls() {
        return walls;
    }



}
