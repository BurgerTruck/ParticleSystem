import java.awt.*;
import java.awt.image.BufferedImage;

public class CanvasBuffer extends BufferedImage {

    public int startX;
    public int startY;
    public int endX;
    public int endY;
    public Graphics2D g2d;
    public CanvasBuffer(int width, int height, int offsetX, int offsetY ) {
        super(width, height, BufferedImage.TYPE_INT_RGB);
        this.startX = offsetX;
        this.startY = offsetY;
        this.g2d = createGraphics();
        this.endX = this.startX + width;
        this.endY = this.startY + height;
//        g2d.setBackground();
//        g2d.setBackground(new Color(0, true));
//        g2d.setBackground(new Color(0, 0, 0, 0));
        g2d.setBackground(Color.WHITE);
    }
}
