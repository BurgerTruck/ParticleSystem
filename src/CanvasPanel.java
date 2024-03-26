import com.sun.corba.se.impl.orbutil.graph.Graph;

import javax.swing.*;
import java.awt.*;
import java.awt.Point;
import java.awt.event.*;
import java.awt.image.*;

public class CanvasPanel extends JPanel {


    private JPanel fpsPanel;
    private JLabel fpsLabel;
    private final BufferedImage buffer;
    private final BufferedImage frontBuffer;
    private Controller controller;
    private Graphics2D[] bufferGraphics;
    private int pixelSize;
    public CanvasPanel(int width, int height){
        super(true);
        Dimension d = new Dimension(width, height);
        setSize(d);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);
        setBackground(Color.WHITE);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        fpsPanel = new JPanel();
        fpsLabel = new JLabel();
        fpsPanel.add(fpsLabel);
        fpsPanel.setAlignmentX(RIGHT_ALIGNMENT);
        fpsPanel.setMaximumSize(new Dimension(30,20));
        add(fpsPanel);


        buffer = new BufferedImage(GUI.canvasWidth, GUI.canvasHeight, BufferedImage.TYPE_INT_RGB);
        frontBuffer = new BufferedImage(GUI.canvasWidth, GUI.canvasHeight, BufferedImage.TYPE_INT_RGB);

        bufferGraphics = new Graphics2D[Config.NUM_THREADS];
        for(int i = 0; i < Config.NUM_THREADS; i++){
            bufferGraphics[i] = buffer.createGraphics();
            bufferGraphics[i].setBackground(Color.WHITE);
            bufferGraphics[i].setColor(Color.BLACK);
        }

        //fps timer
        new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fpsLabel.setText(String.valueOf(controller.getFrames()*2));
                controller.resetFrames();
            }
        }).start();

        initializeListeners();
//        setBorder(BorderFactory.createLineBorder(Color.BLACK,1));
        pixelSize = (int) ((double)GUI.canvasWidth/Config.eWidth);
        if((pixelSize&1)==0)pixelSize++;

    }

    public void drawParticle(Particle p, int bufferGraphicsIndex){
        int halfWidth  = Config.halfParticleWidth;
        int halfHeight = Config.halfParticleHeight;
        if(controller.isExplorer()){
            halfWidth = Config.halfEParticleWidth;
            halfHeight = Config.halfEParticleHeight;
        }
        drawRectangle(p.p, bufferGraphics[bufferGraphicsIndex], halfWidth, halfHeight );
    }
    private void fillRect(int x, int y, int width, int height, Graphics2D g){

        g.fillRect(x, y, width, height);
    }
    public void drawFrontBuffer(){
        frontBuffer.getGraphics().drawImage(buffer, 0,0, null);
    }
    private boolean drawRectangle(Position p, Graphics2D g, int halfWidth, int halfHeight){
        int x;
        int y;
        int[] translatedPosition = controller.transformLocalPosition(p.x, p.y);
        x = translatedPosition[0];
        y = translatedPosition[1];

        y = getHeight() - y-1;
        int endX = x+halfWidth;
        int endY = y+halfHeight;
        x = x-halfWidth;
        y = y - halfHeight;

        if(x >=GUI.canvasWidth || endX < 0 || y >=GUI.canvasHeight || endY < 0)
            return false;

        endX = clamp(endX, 0, GUI.canvasWidth-1);
        endY = clamp(endY, 0, GUI.canvasHeight-1);
        x = clamp(x, 0, GUI.canvasWidth-1);
        y = clamp(y, 0, GUI.canvasHeight-1);

        int width = endX - x+1;
        int height = endY - y+1;

        int midX = endX + x >>1;
        int midY = endY + y >>1;

        if(!controller.isExplorer()){
            if(   buffer.getRGB(midX, midY )!=-1)return false;
        }else{
            int countFilled = 0;
            int[] yValues = new int[]{y, midY, endY};
            int[] xValues = new int[]{x, midX, endX};
            for(int i=  0; i < 3; i++){
                for(int j = 0; j <3; j++){
                    if(buffer.getRGB(xValues[j], yValues[i])!=-1)countFilled++;
                }
            }
            if(countFilled == 9 )return false   ;
        }

        fillRect(x, y, width, height, g);

        return true;
    }
    private int clamp(int val, int min, int max){
        return Math.min(max, Math.max(val, min));
    }
    private void drawRectangle(Position[] positions, Graphics2D g2){
        Position topLeft = positions[0];
        Position bottomRight = positions[1];
        int[] topLeftTransformed = controller.transformLocalPosition(topLeft.x, topLeft.y   );
        int[] bottomRightTransformed = controller.transformLocalPosition(bottomRight.x, bottomRight.y);

        int startX = topLeftTransformed[0];
        int startY = topLeftTransformed[1];

        int endX = bottomRightTransformed[0];
        int endY = bottomRightTransformed[1];

        if(startX >=GUI.canvasWidth || endX < 0 || startY <0 || endY >=GUI.canvasHeight)
            return ;
        
        endX = clamp(endX, 0, GUI.canvasWidth-1);
        endY = clamp(endY, 0, GUI.canvasHeight-1);
        startX = clamp(startX, 0, GUI.canvasWidth-1);
        startY = clamp(startY, 0, GUI.canvasHeight-1);

        int width = Math.abs(endX - startX+1);
        int height = Math.abs(endY - startY+1);

        startY = getHeight()-startY -1;


        fillRect(startX, startY, width, height, g2 );
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

        for(Kirby kirby: controller.getKirbies()){
            if(kirby==controller.getPlayerKirby())continue;
            drawKirby(g2, kirby);
        }

        drawKirby(g2, controller.getPlayerKirby());
        drawBounds(g2);
    }
    private void drawKirby(Graphics2D g, Kirby kirby){
        if(kirby==null) return;
        double x = kirby.getX();
        double y = kirby.getY();
        if(!controller.inViewBox( x,  y, Config.halfKirbyWidth, Config.halfKirbyHeight)) return;
        System.out.println("DRAWING KIRBY");
        int[] localPosition = controller.transformLocalPosition(x, y);
        int drawX = localPosition[0];
        int drawY = localPosition[1];

        int halfWidth = Config.halfKirbyWidth;
        int halfHeight = Config.halfKirbyHeight;
        if(controller.isExplorer()) {
            halfWidth = Config.halfEKirbyWidth;
            halfHeight = Config.halfEKirbyHeight;
        }
        drawY = getHeight() - drawY -1;
        if(controller.isExplorer()){
            kirby.drawSprite(g, drawX - halfWidth, drawX + halfWidth,drawY - halfHeight, drawY + halfHeight );
        }else{
            g.setColor(Color.decode("#eb81a6"));
            fillRect(drawX - halfWidth, drawY - halfHeight, Config.kirbyWidth, Config.kirbyHeight, g);
        }

    }
    private void drawBounds(Graphics2D g){
        if(controller.isExplorer()){
            drawRectangle(Config.bottomBoundRect, g);
            drawRectangle(Config.leftBoundRect, g);
            drawRectangle(Config.rightBoundRect, g);
            drawRectangle(Config.topBoundRect, g);
        }

    }
    private boolean leftClicked = false;
    private Point  clicked = null;



    private void initializeListeners(){
        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                controller.keyInput(e, true);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                controller.keyInput(e, false);
            }
        });

        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {}
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocus();
                if(controller.isExplorer()) return;

                if(SwingUtilities.isLeftMouseButton(e)){
                    leftClicked = true;

                    Point mouse = e.getLocationOnScreen();
                    SwingUtilities.convertPointFromScreen(mouse, CanvasPanel.this);
                    clicked = new Point(mouse.getLocation().x,  mouse.getLocation().y);
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if(controller.isExplorer())return;
                Point mouse = e.getLocationOnScreen();
                SwingUtilities.convertPointFromScreen(mouse, CanvasPanel.this);

                if(leftClicked){
                    leftClicked = false;
                    controller.addParticle(new Particle(new Position(clicked.x, getHeight()-clicked.y), clicked.distance(mouse ), Math.toDegrees(Math.atan2((getHeight()-mouse.y)-(getHeight()-clicked.y),mouse.x-clicked.x))));
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
