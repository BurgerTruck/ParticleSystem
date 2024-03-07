import javax.swing.*;
import java.awt.*;

public class Kirby {
    public static final Image SPRITE_SHEET = new ImageIcon("kirby_what.png").getImage();
    public static final int FRAME_WIDTH = 30;
    public static final int FRAME_HEIGHT = 30;

    public static final int NUM_FRAMES_IDLE = 4;
    public static final int NUM_FRAMES_WALKING = 10;



    private double spriteSpeedX = 30;
    private double spriteSpeedY = 30;
    private boolean isWalking = false;

    private int frameCol = 0;
    private int frameRow = 0;

    private double x = GUI.canvasWidth/2;
    private double y = GUI.canvasHeight/2;


    private double animationSeconds = 0;
    private static double[] walkingFrameDurations;
    private static double[] idleFrameDurations;

    public boolean wHeld = false;
    public boolean aHeld = false;
    public boolean sHeld = false;
    public boolean dHeld = false;

    static{
        walkingFrameDurations = new double[NUM_FRAMES_WALKING];
        idleFrameDurations = new double[NUM_FRAMES_IDLE];
        for(int i = 0; i < NUM_FRAMES_WALKING; i++) walkingFrameDurations[i] = 0.08;
        for(int i = 0; i < NUM_FRAMES_IDLE; i++) idleFrameDurations[i]=0.15;


        idleFrameDurations[0] = 3;
    }
    private boolean horizontalFlipped = false;
    public void updateDirectionsHeld(boolean w, boolean a, boolean s, boolean d){
        wHeld = w;
        aHeld = a;
        sHeld = s;
        dHeld = d;
        if(w || a || s || d) setWalking( true);
        else setWalking(false);

        if(d )horizontalFlipped = false;
        else if(a) horizontalFlipped = true;

    }
    private void setWalking(boolean walking){

        if(walking !=isWalking){
            frameCol = 0;
            animationSeconds = 0;
        }
        isWalking = walking;
        if(isWalking)frameRow = 1;
        else frameRow = 0;



    }
    public void updateAnimation(double elapsed){
        int frameLength = isWalking?NUM_FRAMES_WALKING:NUM_FRAMES_IDLE;
        double[] frameDurations = isWalking?walkingFrameDurations:idleFrameDurations;
        double duration = frameDurations[frameCol];
        animationSeconds+=elapsed;
        if(animationSeconds>=duration){
            frameCol = (frameCol+1)%frameLength;
            animationSeconds = 0;
            if(!isWalking && frameCol==1){
                frameDurations[0] = Math.random() *5;
            }
        }


    }

    public void drawSprite(Graphics2D g, int DRAW_X, int DRAW_END_X, int DRAW_Y, int DRAW_END_Y){
        int sx = frameCol * FRAME_WIDTH;
        int sy = frameRow * FRAME_WIDTH;
        int startX = DRAW_X, endX = DRAW_END_X;
        if(horizontalFlipped){
            startX = DRAW_END_X;
            endX = DRAW_X;
        }
        g.drawImage(SPRITE_SHEET, startX, DRAW_Y, endX, DRAW_END_Y,
                sx, sy, sx + FRAME_WIDTH, sy + FRAME_HEIGHT, null   );
    }

    public void updateSpritePosition(double elapsed){
        if(wHeld)y +=spriteSpeedY * elapsed;
        if(aHeld)x -=spriteSpeedX*elapsed;
        if(sHeld)y -=spriteSpeedY * elapsed;
        if(dHeld)x +=spriteSpeedY*elapsed;


    }
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }
}
