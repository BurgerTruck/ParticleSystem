import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class World implements Serializable {
    private ArrayList<Particle> particles;
    public HashMap<Integer, Kirby> kirbies;

    private transient Controller controller;
    private static  Thread[] threads = new Thread[Config.NUM_THREADS];

    public World(ArrayList<Particle> particles, HashMap<Integer, Kirby> kirbies){
        this.particles = particles;
        this.kirbies = kirbies;
    }
    public World(Controller controller){
        particles = new ArrayList<>();
        kirbies = new HashMap<>();

        setController(controller);
    }

    public ArrayList<Particle> getParticles() {
        return particles;
    }

    public void addKirby(int id, Kirby kirby){
        kirbies.put(id,  kirby);
    }
    public void update(double elapsed){
        updateParticlesAndDrawToBuffer(elapsed);
        joinThreads();
        for(Kirby kirby: getKirbies()){
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
                    particle.move( elapsed);
                    controller.getCanvas().drawParticle(particle, finalI);
                }
            });
            threads[i].start();
        }
    }

    public Collection<Kirby> getKirbies() {
        return kirbies.values();
    }

    public void setController(Controller controller) {
        this.controller = controller;
        controller.setWorld(this);
    }

    public Kirby getKirby(int id){
        return kirbies.get(id);
    }

    public void removeKirby(int id  ){
        kirbies.remove(id);
    }
}
