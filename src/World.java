import java.util.ArrayList;

public class World {
    private ArrayList<Particle> particles;
    private ArrayList<Wall> walls;
    private Controller controller;
    private ArrayList<Kirby> kirbies;
    private Thread[] threads;
    public World(Controller controller){
        particles = new ArrayList<>();
        walls = new ArrayList<>();
        this.controller = controller;
        kirbies = new ArrayList<>();
        threads = new Thread[Config.NUM_THREADS];
        controller.setWorld(this);

    }

    public ArrayList<Particle> getParticles() {
        return particles;
    }

    public ArrayList<Wall> getWalls() {
        return walls;
    }

    public void addKirby(Kirby kirby){
        kirbies.add(kirby);
    }
    public void update(double elapsed){
        updateParticlesAndDrawToBuffer(elapsed);
        joinThreads();
        for(Kirby kirby: kirbies){
            kirby.updateSpritePosition(elapsed);
            kirby.updateAnimation(elapsed);
        }
    }
    private void joinThreads(){
        for (Thread thread : threads) {
            if(thread==null) break;
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private void updateParticlesAndDrawToBuffer(double elapsed){

        for (int i = 0; i < Config.NUM_THREADS; i++) {
            if(i >=getParticles().size()) break;
            int finalI = i;
            threads[i] = new Thread(() -> {
                for (int j = finalI; j < getParticles().size(); j += Config.NUM_THREADS) {
                    Particle particle = getParticles().get(j);
                    particle.move(getWalls(), elapsed);
                    controller.getCanvas().drawParticle(particle, finalI);
                }
            });
            threads[i].start();
        }
    }

    public ArrayList<Kirby> getKirbies() {
        return kirbies;
    }
}
