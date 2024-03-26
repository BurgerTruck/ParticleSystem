import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

public class Kirby implements Serializable {
    public static final BufferedImage SPRITE_SHEET;


    public static final int FRAME_WIDTH = 30;
    public static final int FRAME_HEIGHT = 30;

    public static final int NUM_FRAMES_IDLE = 4;
    public static final int NUM_FRAMES_WALKING = 10;


    private double spriteSpeedX = 30;
    private double spriteSpeedY = 30;
    private boolean isWalking = false;

    private int frameCol = 0;
    private int frameRow = 0;

    private Position p = new Position(GUI.canvasWidth/2, GUI.canvasHeight/2);


    private double animationSeconds = 0;
    private static double[] walkingFrameDurations;
    private static double[] idleFrameDurations;

    private Color color;
    private transient BufferedImage tintedSheet;
    public boolean wHeld = false;
    public boolean aHeld = false;
    public boolean sHeld = false;
    public boolean dHeld = false;
    private boolean horizontalFlipped = false;
    static{
        try {
            SPRITE_SHEET = ImageIO.read((Kirby.class.getResource("kirby_gray.png")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        walkingFrameDurations = new double[NUM_FRAMES_WALKING];
        idleFrameDurations = new double[NUM_FRAMES_IDLE];
        for(int i = 0; i < NUM_FRAMES_WALKING; i++) walkingFrameDurations[i] = 0.08;
        for(int i = 0; i < NUM_FRAMES_IDLE; i++) idleFrameDurations[i]=0.15;


        idleFrameDurations[0] = 3;
    }
    public Kirby(Color tintColor) {

        this.color = tintColor;
        initializeColor();
    }
    public void initializeColor(){
        if(this.color==null){
            try {
                tintedSheet   = ImageIO.read((Kirby.class.getResource("kirby_what.png")));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        tintedSheet = new BufferedImage(SPRITE_SHEET.getWidth(), SPRITE_SHEET.getHeight(), BufferedImage.TYPE_INT_ARGB );
        for (int x = 0; x < SPRITE_SHEET.getWidth(); x++) {
            for (int y = 0; y < SPRITE_SHEET.getHeight(); y++) {
                int rgb = SPRITE_SHEET.getRGB(x, y);
                int newRGB = getNewRGB(this.color, rgb);
                tintedSheet.setRGB(x, y, newRGB);
            }
        }
    }

    private static int getNewRGB(Color tintColor, int rgb) {

        int alpha = (rgb >> 24) & 0xFF;
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        if(alpha==0) return rgb;
//        // Apply tint color
        red = (int) ((double)red / 0XBC* tintColor.getRed() );
        green = (int) ((double)green / 0xBC* tintColor.getGreen() );
        blue = (int) ((double)blue / 0xBC* tintColor.getBlue() );

        if(rgb==0XFFFFFFFF){
            return 0XFFFFFFFF;
        }
        // Set new pixel value

        int newRGB = (alpha << 24) | (red << 16) | (green << 8) | blue;
        return newRGB;

//        float HSV[] = new float[3];
//        Color.RGBtoHSB(red,green,blue, HSV);
//        float tintHSV[] = new float[3]  ;
//        Color.RGBtoHSB(tintColor.getRed(),tintColor.getGreen(), tintColor.getBlue(),tintHSV);
//        int ret = Color.getHSBColor(tintHSV[0], HSV[1], HSV[2]).getRGB();
//        ret = ret & (alpha<<24);
//        return ret;
    }


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
        int padding = 0;
        double ratio = ((double)padding/FRAME_WIDTH);
        int size = DRAW_END_Y - DRAW_Y+1;
        padding = (int) Math.ceil(ratio*size);
        int startX = DRAW_X-padding, endX = DRAW_END_X+padding;
        if(horizontalFlipped){
            startX = DRAW_END_X+padding;
            endX = DRAW_X-padding;
        }
        g.drawImage(tintedSheet, startX, DRAW_Y-padding, endX, DRAW_END_Y + padding,
                sx, sy, sx + FRAME_WIDTH, sy + FRAME_HEIGHT, null   );

    }

    public void updateSpritePosition(double elapsed){
        if(wHeld)p.y +=spriteSpeedY * elapsed;
        if(aHeld)p.x -=spriteSpeedX*elapsed;
        if(sHeld)p.y -=spriteSpeedY * elapsed;
        if(dHeld)p.x +=spriteSpeedY*elapsed;

        p.x = Math.max(Config.halfKirbyWidth, Math.min(p.x, GUI.canvasWidth - Config.halfKirbyWidth-1));
        p.y = Math.max(Config.halfKirbyHeight, Math.min(p.y, GUI.canvasHeight - Config.halfKirbyHeight-1));
    }
    public double getX() {
        return p.x;
    }

    public double getY() {
        return p.y;
    }

    public void setX(double x) {
        p.x = x;
    }

    public void setY(double y) {

        p.y = y;
    }

    public Position getPosition() {
        return p;
    }

    public void setPosition(Position p) {
        this.p = p;
    }
}
