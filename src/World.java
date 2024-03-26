import com.sun.javafx.image.ByteToBytePixelConverter;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.*;

public class World implements Serializable {
    private ArrayList<Particle> particles;
    public  HashMap<Integer, Kirby> kirbies;
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

    public byte[] toBytes(){
        int particlesSize = particles.size()    ;
        int kirbySize = kirbies.size();
        //Size of particles -> particles -> Size of hashmap -> Each entry in hashmap
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + particlesSize * Particle.numBytes() +Integer.BYTES +  kirbySize * (Integer.BYTES + Kirby.numBytes() ) );
        buffer.putInt(particlesSize );
        for(int i = 0; i < particlesSize; i++){
            buffer.put(particles.get(i).toBytes());
        }
        buffer.putInt(kirbySize );
        Set<Map.Entry<Integer, Kirby>> entries = kirbies.entrySet();
        for(Map.Entry<Integer, Kirby> entry: entries){
            buffer.putInt(entry.getKey());
            buffer.put(entry.getValue().toBytes()   );
        }

        return buffer.array();
    }

    public static World decodeBytes(byte[] bytes){
        ByteBuffer buffer = ByteBuffer.wrap(bytes   );
        int numParticles = buffer.getInt();
        ArrayList<Particle >  particles = new ArrayList<>(  );
        for(int i = 0; i < numParticles; i++){
            byte[] pBuffer = new byte[Particle.numBytes()];
            buffer.get(pBuffer  );
            Particle particle = Particle.decodeBytes(pBuffer);
            particles.add(particle  );
        }

        HashMap<Integer, Kirby> kirbies = new HashMap<>(    );

        int numKirbies = buffer.getInt()    ;
        for(int i= 0; i < numKirbies; i++){
            int id = buffer.getInt()    ;

            byte[] kirbyBuffer = new byte[Kirby.numBytes()];
            buffer.get(kirbyBuffer);
            Kirby kirby = Kirby.decodeBytes(kirbyBuffer );

            kirbies.put(id, kirby);
        }
        World ret = new World(particles, kirbies    );
        return ret;
    }
}
