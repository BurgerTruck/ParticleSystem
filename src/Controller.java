import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Controller implements Runnable{
    private Kirby playerKirby;
    private long prevStart = -1;
    private double bottomLeftX;
    private double bottomLeftY;
    private double topRightX ;
    private double topRightY ;
    private boolean isExplorer;
    protected World world;
    protected CanvasPanel canvas;
    protected int frames = 0;

    private boolean wHeld;
    private boolean aHeld;
    private boolean dHeld;
    private boolean sHeld;
    public Controller(){
        isExplorer = false;
//        updateViewBox();
    }

    protected double getElapsed(){
        long curr = System.nanoTime();

        double elapsed =   (curr - prevStart) / 1000000000d;
        prevStart = curr;
        return elapsed;
    }

    public void addParticle(Particle p){
        world.getParticles().add(p);
    }
    public void addParticles(java.util.List<Particle> particleList){
        world.getParticles().addAll(particleList);
    }
    public void setExplorer(boolean isExplorer){
        this.isExplorer = isExplorer;
        updateViewBox();
    }

    protected void updateViewBox(){
        if(isExplorer()) {
            bottomLeftX = ( playerKirby.getX() - Config.halfEWidth);
            bottomLeftY =  (playerKirby.getY() - Config.halfEHeight);
            topRightX =  (playerKirby.getX() + Config.halfEWidth);
            topRightY =  (playerKirby.getY() + Config.halfEHeight);
        }else{
            bottomLeftX = 0;
            bottomLeftY = 0;
            topRightX = GUI.canvasWidth-1;
            topRightY = GUI.canvasHeight-1;
        }
    }

    public boolean inViewBox(double x, double y, int halfWidth, int halfHeight){
        if(x + halfWidth < bottomLeftX-1) return false;
        if(x - halfWidth> topRightX+1) return false;
        if(y + halfHeight < bottomLeftY-1) return false;
        if(y - halfHeight > topRightY+1) return false;
        return true;
    }

    public static boolean inViewBox(Position kirbyPosition, Position entityPosition, int halfWidth, int halfHeight){
        double bottomLeftX = (kirbyPosition.x - Config.halfEWidth);
        double bottomLeftY =  (kirbyPosition.y - Config.halfEHeight);
        double topRightX =  (kirbyPosition.x + Config.halfEWidth);
        double topRightY =  (kirbyPosition.y + Config.halfEHeight);

        double x = entityPosition.x;
        double y = entityPosition.y;


        if(x + halfWidth < bottomLeftX-1) return false;

        if(x - halfWidth> topRightX+1) return false;

        if(y + halfHeight < bottomLeftY-1) return false;
        if(y - halfHeight > topRightY+1) return false;

        return true;
    }
    public void keyInput(KeyEvent e, boolean pressed){
        if(e.getKeyCode()==KeyEvent.VK_W) wHeld = pressed;
        if(e.getKeyCode() == KeyEvent.VK_A) aHeld = pressed;
        if(e.getKeyCode() == KeyEvent.VK_D) dHeld = pressed;
        if(e.getKeyCode() ==KeyEvent.VK_S) sHeld = pressed;
    }


    public boolean isExplorer() {
        return isExplorer;
    }

    public int getBottomLeftY() {
        return (int) bottomLeftY;
    }

    public int getBottomLeftX() {
        return (int) bottomLeftX;
    }

    public int getTopRightX() {
        return (int) topRightX;
    }

    public int getTopRightY() {
        return (int) topRightY;
    }

    public List<Particle> getParticles(){
        return world.getParticles();
    }

    public void resetFrames(){
        frames= 0 ;
    }

    public int getFrames() {
        return frames;
    }

    public int[] transformLocalPosition(double x, double y){
        int width;
        int height;
        if(isExplorer()){
            width = Config.eWidth;
            height = Config.eHeight;
        }else{
            width = GUI.canvasWidth;
            height = GUI.canvasHeight;
        }
        int newX = (int) ((x - bottomLeftX) / (width-1)  *  (GUI.canvasWidth-1));
        int newY= (int) ((y - bottomLeftY)/ (height-1) * (GUI.canvasHeight-1));

        return new int[]{newX,  newY    };
    }

    public Kirby getPlayerKirby() {
        return playerKirby;
    }

    public void setCanvas(CanvasPanel canvas) {
        this.canvas = canvas;
    }
    public void start(){
        prevStart = System.nanoTime();
        new Thread(this).start();
    }
    public void setWorld(World world) {
        this.world = world;
    }
    public void update() throws IOException, InterruptedException {
        canvas.clearBackBuffer();
        if(playerKirby!=null) playerKirby.updateDirectionsHeld(wHeld, aHeld,sHeld, dHeld);

        world.update(getElapsed());
        updateViewBox();
        canvas.drawParticles();
        canvas.drawKirbies();

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
    @Override
    public void run() {
        while(true){
            try {
                update();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public Collection<Kirby> getKirbies(){
        if(world!=null)return world.getKirbies();
        return new ArrayList<>();
    }

    public CanvasPanel getCanvas() {
        return canvas;
    }

//    public void addKirby(Kirby kirby){
//        world.addKirby(kirby);
//    }


    public void setPlayerKirby(Kirby playerKirby) {
        this.playerKirby = playerKirby;
    }

    public boolean iswHeld() {
        return wHeld;
    }

    public boolean isaHeld() {
        return aHeld;
    }

    public boolean isdHeld() {
        return dHeld;
    }

    public boolean issHeld() {
        return sHeld;
    }
    public void updateInput(int kirbyId, Input input){
        Kirby kirby = world.getKirby(kirbyId);
        if(kirby == null)return;
        kirby.updateDirectionsHeld(input.w, input.a, input.s, input.d);
    }

}
