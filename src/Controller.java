import javax.swing.*;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.security.Key;
import java.util.ArrayList;

public class Controller {
    private Kirby playerKirby;
    private ArrayList<Kirby> otherKirbies;

    private long prevStart = -1;
    private int bottomLeftX;
    private int bottomLeftY;
    private int topRightX ;
    private int topRightY ;
    private boolean isExplorer;
    private World world;
    private CanvasPanel canvas;
    private int frames = 0;
    private Thread[] threads;
    public Controller(World world, GUI gui  ){
        this.world = world;
        this.canvas = gui.getCanvas();
        gui.setController(this);

        playerKirby  = new Kirby()  ;
        otherKirbies = new ArrayList<>();
        threads = new Thread[Config.NUM_THREADS];

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    canvas.clearBackBuffer();
                    double elapsed = getElapsed();
                    updateParticlesAndDrawToBuffer(elapsed);
                    joinThreads();
                    playerKirby.updateAnimation(elapsed);
                    playerKirby.updateSpritePosition(elapsed);
                    canvas.drawFrontBuffer();
                    try {
                        SwingUtilities.invokeAndWait(new Runnable() {
                            @Override
                            public void run() {
                                canvas.repaint();
                            }
                        });
                    } catch (InterruptedException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                    frames++;
                }
            }
        }).start();
    }

    private double getElapsed(){
        long curr = System.nanoTime();

        double elapsed =   (curr - prevStart) / 1000000000d;
        prevStart = curr;
        return elapsed;
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
            if(i >=world.getParticles().size()) break;
            int finalI = i;
            threads[i] = new Thread(() -> {
                for (int j = finalI; j < world.getParticles().size(); j += Config.NUM_THREADS) {
                    Particle particle = world.getParticles().get(j);
                    particle.move(world.getWalls(), elapsed);
                    canvas.drawParticle(particle, finalI);
                }
            });
            threads[i].start();
        }
    }

    public void addParticle(Particle p){
        world.getParticles().add(p);
    }
    public void addParticles(java.util.List<Particle> particleList){
        world.getParticles().addAll(particleList);
    }
    public void addWall(Wall wall){
        world.getWalls().add(wall);
        canvas.drawWall( wall);
    }

    public void setExplorer(boolean isExplorer){
        this.isExplorer = isExplorer;
        updateViewBox();
    }

    private void updateViewBox(){
        if(isExplorer) {
            bottomLeftX = (int) (playerKirby.getX() - Config.halfEWidth);
            bottomLeftY = (int) (playerKirby.getY() - Config.halfEHeight);
            topRightX = (int) (playerKirby.getX() + Config.halfEWidth);
            topRightY = (int) (playerKirby.getY() + Config.halfEHeight);
        }
    }

    public boolean inViewBox(int x, int y, int halfWidth, int halfHeight){
        if(x + halfWidth < bottomLeftX) return false;
        if(x - halfWidth> topRightX+1) return false;
        if(y + halfHeight < bottomLeftY) return false;
        if(y - halfHeight > topRightY+1) return false;
        return true;
    }
    public void keyPressed(KeyEvent e){
        if(e.getKeyCode()==KeyEvent.VK_W) playerKirby.wHeld = true;
        if(e.getKeyCode() == KeyEvent.VK_A) playerKirby.aHeld = true;
        if(e.getKeyCode() == KeyEvent.VK_D) playerKirby.dHeld = true;
        if(e.getKeyCode() ==KeyEvent.VK_S) playerKirby.sHeld = true;
    }
    public void keyReleased(KeyEvent e){
        if(e.getKeyCode()==KeyEvent.VK_W) playerKirby.wHeld = false;
        if(e.getKeyCode() == KeyEvent.VK_A) playerKirby.aHeld = false;
        if(e.getKeyCode() == KeyEvent.VK_D) playerKirby.dHeld = false;
        if(e.getKeyCode() ==KeyEvent.VK_S) playerKirby.sHeld = false;
    }

    public boolean isExplorer() {
        return isExplorer;
    }

    public int getBottomLeftY() {
        return bottomLeftY;
    }

    public int getBottomLeftX() {
        return bottomLeftX;
    }

    public int getTopRightX() {
        return topRightX;
    }

    public int getTopRightY() {
        return topRightY;
    }

    public ArrayList<Particle> getParticles(){
        return world.getParticles();
    }

    public void resetFrames(){
        frames= 0 ;
    }

    public int getFrames() {
        return frames;
    }

    public int[][]
}
