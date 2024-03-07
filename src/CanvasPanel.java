import javax.swing.*;
import java.awt.*;
import java.awt.Point;
import java.awt.event.*;
import java.awt.image.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class CanvasPanel extends JPanel {


    private JPanel fpsPanel;
    private JLabel fpsLabel;
    private final BufferedImage buffer;
    private final BufferedImage frontBuffer;
    private Controller controller;
    private Graphics2D[] bufferGraphics;
    private int pixelSize;
    private Kirby kirby;
    public CanvasPanel(int width, int height){
        super(true);
        Dimension d = new Dimension(width, height);
        setSize(d);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);
        setBackground(Color.WHITE);
        this.controller = controller;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        fpsPanel = new JPanel();
        fpsLabel = new JLabel();
        fpsPanel.add(fpsLabel);
        fpsPanel.setAlignmentX(RIGHT_ALIGNMENT);
        fpsPanel.setMaximumSize(new Dimension(30,20));
        add(fpsPanel);


        buffer = new BufferedImage(GUI.canvasWidth, GUI.canvasHeight, BufferedImage.TYPE_BYTE_GRAY);
        frontBuffer = new BufferedImage(GUI.canvasWidth, GUI.canvasHeight, BufferedImage.TYPE_BYTE_GRAY);

        bufferGraphics = new Graphics2D[Config.NUM_THREADS];
        for(int i = 0; i < Config.NUM_THREADS; i++){
            bufferGraphics[i] = buffer.createGraphics();
            bufferGraphics[i].setBackground(Color.WHITE);
            bufferGraphics[i].setColor(Color.BLACK);
        }

        kirby = new Kirby();


        //fps timer
        new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fpsLabel.setText(String.valueOf(controller.getFrames()*2));
                controller.resetFrames();
            }
        }).start();

        initializeListeners();
        setBorder(BorderFactory.createLineBorder(Color.BLACK,1));

        pixelSize = (int) ((double)GUI.canvasWidth/Config.eWidth);
        if((pixelSize&1)==0)pixelSize++;


    }




    public void drawWall(Wall wall){
//        g.drawLine((int) wall.p1.x, (int) (getHeight()- wall.p1.y), (int) wall.p2.x, (int) (getHeight()-wall.p2.y));
        int x2 = (int) wall.p2.x, x1 = (int) wall.p1.x;
        int y2 = getHeight() - (int)  wall.p2.y,  y1 =  getHeight() - (int) wall.p1.y;
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;
        int e2;

        while (x1 != x2 || y1 != y2) {
            for (int i = -halfParticleHeight; i <= halfParticleHeight; i++) {
                for (int j = -halfParticleWidth; j <= halfParticleWidth; j++) {
                    int newX = x1 + j;
                    int newY = y1 + i;
                    if (newX >= 0 && newX < GUI.canvasWidth && newY >= 0 && newY < GUI.canvasHeight) {
//                        backPixels[newY * GUI.canvasWidth + newX] = 1 ;
                    }else break;
                }
            }
            e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }

    }

    private final int particleWidth = 3;
    private final int particleHeight = 3;


    private final int halfParticleWidth = particleWidth>>1;
    private final int halfParticleHeight = particleHeight>>1;

    private int eParticleWidth = (int) ((double)GUI.canvasWidth/Config.eWidth * particleWidth);
    private int eParticleHeight = eParticleWidth;

    public void drawParticle(Particle p, int bufferGraphicsIndex){
        drawPixel(p.p, bufferGraphics[bufferGraphicsIndex]);
    }
    private void fillRect(int x, int y, int width, int height, Graphics2D g){

        g.fillRect(x, y, width, height);
    }
    public void drawFrontBuffer(){

        Graphics2D g = (Graphics2D) frontBuffer.getGraphics();
        if(!controller.isExplorer()){
           g.drawImage(buffer, 0,0, null);
        }else{
            synchronized (frontBuffer){
                g.setBackground(Color.WHITE);
                g.clearRect(0,0, GUI.canvasWidth, GUI.canvasHeight);
                int row = 0;
                int col = 0;
                for(int y = controller.getBottomLeftY(); y<=controller.getTopRightY(); y++, row+=pixelSize){
                    for(int x = controller.getBottomLeftX(); x<=controller.getTopRightX(); x++, col+=pixelSize){
                        if(x < 0 || y < 0 || x>=GUI.canvasWidth || y>=GUI.canvasHeight){
                            g.setColor(Color.BLACK);
                            g.fillRect(col, getHeight()- row - pixelSize, pixelSize, pixelSize);
                            continue;
                        }
                        if(buffer.getRGB(x, getHeight()- y-1)!=-1){
                            g.setColor(Color.BLACK);
                            g.fillRect(col, getHeight()- row-pixelSize, pixelSize, pixelSize);
                        }
                    }
                    col = 0;
                }
            }

        }
    }
    private boolean drawPixel(Position p, Graphics2D g){
        int x = (int)(p.x);
        int y = (int)(p.y);

        int width = particleWidth;
        int height = particleHeight;

        int halfHeight = height>>1;
        int halfWidth = width>>1;
        if(!controller.inViewBox(x, y, halfWidth, halfHeight)) return false;
        y = getHeight() - y;

        int endX = Math.min(x + halfWidth, GUI.canvasWidth-1);
        int endY = Math.min(y + halfHeight, GUI.canvasHeight-1);
        x = Math.max(0, Math.min(x - halfWidth, GUI.canvasWidth - 1));
        y = Math.max(0, Math.min(y - halfHeight, GUI.canvasHeight - 1));
        endY = Math.max(0, endY);
        endX = Math.max(0, endX);
        width = endX - x+1;
        height = endY - y+1;
        if(x >=GUI.canvasWidth || endX < 0 || y >=GUI.canvasHeight || endY < 0) return false;
        int midX = endX + x >>1;
        int midY = endY + y >>1;
        if(buffer.getRGB(midX, midY )!=-1)return false;
        fillRect(x, y, width, height, g);
        return true;
    }



    private BasicStroke stroke = new BasicStroke(3);

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        if(controller.isExplorer()){
            synchronized (frontBuffer){
                g2.drawImage(frontBuffer, null, 0,0);
            }
        }else g2.drawImage(frontBuffer, null, 0,0);

        g2.setStroke(stroke);
        if(clicked!=null){
            Point mouse = MouseInfo.getPointerInfo().getLocation();
            SwingUtilities.convertPointFromScreen(mouse, this);
            g2.drawLine(clicked.x, clicked.y, mouse.x, mouse.y);
        }
        if(!controller.isExplorer()){
            g2.setColor(Color.RED);
            g2.drawRect(controller.getBottomLeftX(), getHeight() - controller.getTopRightY(),Config.eWidth, Config.eHeight);
        }
        if(controller.isExplorer())kirby.drawSprite(g2);

    }
    private void drawKirby(Graphics2D g, Kirby kirby){
        double x = kirby.getX();
        double y = kirby.getY();

        if(!controller.inViewBox((int) x, (int) y, Config.HALF_DRAW_KIRBY_WIDTH, Config.HALF_DRAW_KIRBY_HEIGHT )) return;
        int[] localPosition = controller.translatePositionToLocal(kirby.getX(), kirby.getY());
        int drawX = localPosition[0];
        int drawY = localPosition[1]
        kirby.drawSprite(g, );
    }

    private boolean leftClicked = false;
    private boolean rightClicked = true;
    private Point  clicked = null;



    private void initializeListeners(){
        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                controller.keyPressed(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                controller.keyReleased(e);
            }
        });

        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {}
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocus();
                Point mouse = e.getLocationOnScreen();
                SwingUtilities.convertPointFromScreen(mouse, CanvasPanel.this);
                clicked = new Point(mouse.getLocation().x,  mouse.getLocation().y);
                if(SwingUtilities.isLeftMouseButton(e))leftClicked = true;
                else if(SwingUtilities.isRightMouseButton(e))rightClicked = true;
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                Point mouse = e.getLocationOnScreen();
                SwingUtilities.convertPointFromScreen(mouse, CanvasPanel.this);

                if(leftClicked){
                    leftClicked = false;
                    controller.addParticle(new Particle(new Position(clicked.x, getHeight()-clicked.y), clicked.distance(mouse ), Math.toDegrees(Math.atan2((getHeight()-mouse.y)-(getHeight()-clicked.y),mouse.x-clicked.x))));

                }else if(rightClicked){
                    rightClicked = false;
                    controller.addWall(new Wall(new Position(clicked.x, getHeight()-clicked.y), new Position(mouse.x, getHeight()-mouse.y)));
                }
                clicked = null;
            }
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
        });

    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void clearBackBuffer(){
        bufferGraphics[0].clearRect(0,0, GUI.canvasWidth, GUI.canvasHeight);
    }
}
