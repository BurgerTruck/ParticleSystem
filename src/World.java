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
        updateParticles(elapsed);
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
    private void updateParticles(double elapsed){

        for (int i = 0; i < Config.NUM_THREADS; i++) {
            if(i >=getParticles().size()) break;
            int finalI = i;
            threads[i] = new Thread(() -> {
                for (int j = finalI; j < getParticles().size(); j += Config.NUM_THREADS) {
                    Particle particle = getParticles().get(j);
                    particle.move( elapsed);
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

    //returns array of byte[], splits particles into multiple byte arrays
    public  byte[][] toBytes(int[] maxLength)  {

        int numParticles = particles.size()    ;
        int numKirbies = kirbies.size();

        int numParticleFragments = numParticles/Config.NUM_PARTICLES_PER_PACKET;
        if(numParticles%Config.NUM_PARTICLES_PER_PACKET>0) numParticleFragments++;
        int remainingParticles = numParticles;
        int pIndex = 0;
        int kirbyMapSize = Integer.BYTES*2 +  numKirbies * (Integer.BYTES + Kirby.numBytes() );
        maxLength[0] = kirbyMapSize;

        byte[][] ret = new byte[numParticleFragments+1][];
        //if first int is 0, then it is array of particles, otherwise kirbies
        ByteBuffer buffer = ByteBuffer.allocate(kirbyMapSize );
        buffer.putInt(1);
        buffer.putInt(numKirbies );
        Set<Map.Entry<Integer, Kirby>> entries = kirbies.entrySet();
        if(numKirbies != entries.size()){
            System.out.println("WHAT KIRBIES NOT RIGHT SIZE");;
        }
        if(numKirbies<=0){
            System.out.println("HEYY");
        }
        for(Map.Entry<Integer, Kirby> entry: entries){
            buffer.putInt(entry.getKey());
            buffer.put(entry.getValue().toBytes()   );
        }



        ret[0] = buffer.array();


        for(int i = 1; i <= numParticleFragments; i++){
            int fragmentSize = Math.min(Config.NUM_PARTICLES_PER_PACKET, remainingParticles);
            ByteBuffer particlesListBuffer = ByteBuffer.allocate(Integer.BYTES*2 + fragmentSize * Particle.numBytes());
            particlesListBuffer.putInt(0);
            particlesListBuffer.putInt(fragmentSize);

            for(int j = 0; j < fragmentSize; j++){
                particlesListBuffer.put(particles.get(pIndex++).toBytes());
            }

            ret[i] = particlesListBuffer.array();
            maxLength[0] = Math.max(maxLength[0], ret[i].length );
            remainingParticles-=Config.NUM_PARTICLES_PER_PACKET ;
        }


//        for(byte[] test: ret){
//            System.out.println(test.length);
//        }
//        System.out.println();
        return ret;
    }

    public static World decodeBytes(byte[][] bytes) {
        ArrayList<Particle> particles = new ArrayList<>();
        HashMap<Integer, Kirby> kirbies = new HashMap<>();
        boolean gotKirbyMap = false;
        for (byte[] b : bytes) {
            ByteBuffer buffer = ByteBuffer.wrap(b);
            int type = buffer.getInt();
            switch (type) {
                case 0:
                    int numParticles = buffer.getInt();
                    byte[] pBuffer = new byte[Particle.numBytes()];
                    for (int i = 0; i < numParticles; i++) {
                        buffer.get(pBuffer);
                        Particle particle = Particle.decodeBytes(pBuffer);
                        particles.add(particle);
                    }
                    //particles array
                    break;
                case 1:
                    gotKirbyMap = true;
                    int numKirbies = buffer.getInt();
                    for (int i = 0; i < numKirbies; i++) {
                        int id = buffer.getInt();
//                        System.out.println("KIRYB ID: "+id);
                        byte[] kirbyBuffer = new byte[Kirby.numBytes()];
                        buffer.get(kirbyBuffer);
                        Kirby kirby = Kirby.decodeBytes(kirbyBuffer);
                        kirbies.put(id, kirby);
                    }

            }
        }
        if(kirbies.isEmpty()){
            System.out.println("HEY STOP");
            System.out.println(gotKirbyMap);
            System.out.println();
        }
        World ret = new World(particles, kirbies);
        return ret;
    }
}
