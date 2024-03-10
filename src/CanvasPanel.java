import javax.swing.*;
import java.awt.*;
import java.awt.Point;
import java.awt.event.*;
import java.awt.image.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class CanvasPanel extends JPanel {
    public static final int NUM_THREADS = 8;

    private ArrayList<Particle> particles;
    private ArrayList<Wall> walls;

    private JPanel fpsPanel;
    private JLabel fpsLabel;

    private Thread[]     threads = new Thread[NUM_THREADS];
    private long prevStart = -1;
    private int frames = 0;

    private final BufferedImage buffer;
    private final BufferedImage frontBuffer;
    private final int length = GUI.canvasWidth * GUI.canvasHeight;
    private boolean isExplorer;

    public static final int eWidth = 33;
    public static final int eHeight = 19;
    private final int halfEWidth = eWidth>>1;
    private final int halfEHeight = eHeight>>1;

    private double spriteX = GUI.canvasWidth/2;
    private double spriteY = GUI.canvasHeight/2;

    private double bottomLeftX = spriteX - halfEWidth;
    private double bottomLeftY = spriteY - halfEHeight;

    private boolean wHeld = false;
    private boolean aHeld = false;
    private boolean sHeld = false;
    private boolean dHeld = false;

    private Graphics2D[] bufferGraphics;

    private double spriteSpeedX = 30;
    private double spriteSpeedY = 30;
    private Kirby kirby;
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
        particles = new ArrayList<>();
        walls = new ArrayList<>();

        buffer = new BufferedImage(GUI.canvasWidth, GUI.canvasHeight, BufferedImage.TYPE_BYTE_GRAY);
        frontBuffer = new BufferedImage(GUI.canvasWidth, GUI.canvasHeight, BufferedImage.TYPE_BYTE_GRAY);

        bufferGraphics = new Graphics2D[NUM_THREADS];
        for(int i = 0; i < NUM_THREADS; i++){
            bufferGraphics[i] = buffer.createGraphics();
            bufferGraphics[i].setBackground(Color.WHITE);
            bufferGraphics[i].setColor(Color.BLACK);
        }

        kirby = new Kirby();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    bufferGraphics[0].clearRect(0,0, GUI.canvasWidth, GUI.canvasHeight);
                    double elapsed = getElapsed();

                    updateParticlesAndDrawToBuffer(elapsed);
                    joinThreads();
                    kirby.updateAnimation(elapsed);
                    updateSpritePosition(elapsed);
                    frontBuffer.getGraphics().drawImage(buffer, 0,0, null);

                    try {
                        SwingUtilities.invokeAndWait(new Runnable() {
                            @Override
                            public void run() {
                                repaint();
                            }
                        });
                    } catch (InterruptedException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                    frames++;
                }
            }
        }).start();

        //fps timer
        new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fpsLabel.setText(String.valueOf(frames*2));
                frames = 0;
            }
        }).start();

        initializeListeners();
        setBorder(BorderFactory.createLineBorder(Color.BLACK,1));

        if((eParticleWidth&1)==0) eParticleWidth++;
        if((eParticleHeight&1)==0) eParticleHeight++;



    }

    public void addParticle(Particle p){
        particles.add(p);
    }
    public void addParticles(java.util.List<Particle> particleList){
        particles.addAll(particleList);
    }

    public void addWall(Wall wall){
        walls.add(wall);
        drawWall( wall);
    }
    private void drawWall(Wall wall){
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

    private int eParticleWidth = (int) ((double)GUI.canvasWidth/eWidth * particleWidth);
    private int eParticleHeight = eParticleWidth;

    private void drawParticle(Particle p, Graphics2D g){
        drawPixel(p.p, g);
    }
    private void fillRect(int x, int y, int width, int height, Graphics2D g){

        g.fillRect(x, y, width, height);
    }
    private boolean drawPixel(Position p, Graphics2D g){

        int width = particleWidth;
        int height = particleHeight;
        int x;
        int y;
        if(isExplorer){
            x = (int) ((p.x - bottomLeftX) / (eWidth-1)  *  (GUI.canvasWidth-1));
            y = (int) ((p.y - bottomLeftY)/ (eHeight-1) * (GUI.canvasHeight-1));
            width = eParticleWidth;
            height = eParticleHeight;
        }else{
            x = (int)   p.x;
            y = (int) p.y;
        }
        y = getHeight() - y;
        int halfHeight = height>>1;
        int halfWidth = width>>1;
        int endX = x+halfWidth;
        int endY = y+halfHeight;
        x = x-halfWidth;
        y = y - halfHeight;
        if(x >=GUI.canvasWidth || endX < 0 || y >=GUI.canvasHeight || endY < 0){
//            System.out.println(x);
//            System.out.println(y);
//            System.out.println("OUT OF SCREEN");
            return false;
        }

        endX = Math.min(endX, GUI.canvasWidth-1);
        endY = Math.min(endY, GUI.canvasHeight-1);
        x = Math.max(0, Math.min(x, GUI.canvasWidth - 1));
        y = Math.max(0, Math.min(y, GUI.canvasHeight - 1));
        endY = Math.max(0, endY);
        endX = Math.max(0, endX);
        width = endX - x+1;
        height = endY - y+1;

        int midX = endX + x >>1;
        int midY = endY + y >>1;
//

        //        int checkIndex = index;

        if(!isExplorer){
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

//        System.out.println(countFilled);

        fillRect(x, y, width, height, g);

        return true;
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

        for (int i = 0; i < NUM_THREADS; i++) {
            if(i >=particles.size()) break;
            int finalI = i;
            threads[i] = new Thread(() -> {
                for (int j = finalI; j < particles.size(); j += NUM_THREADS) {
                    particles.get(j).move(walls, elapsed);
                    drawParticle( particles.get(j), bufferGraphics[finalI]);
                }
            });
            threads[i].start();
        }
    }
    private double getElapsed(){
        long curr = System.nanoTime();

        double elapsed =   (curr - prevStart) / 1000000000d;
        prevStart = curr;
        return elapsed;
    }
    private BasicStroke stroke = new BasicStroke(3);

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(frontBuffer, null, 0,0);
        g2.setStroke(stroke);
        if(clicked!=null){
            Point mouse = MouseInfo.getPointerInfo().getLocation();
            SwingUtilities.convertPointFromScreen(mouse, this);
            g2.drawLine(clicked.x, clicked.y, mouse.x, mouse.y);
        }
        if(!isExplorer){
            g2.setColor(Color.RED);
            g2.drawRect((int) (spriteX - halfEWidth), (int) (getHeight() - (spriteY + halfEHeight)),eWidth, eHeight);
//            g2.fillRect((int) bottomLeftX, (int) (getHeight() - bottomLeftY), 5,5);
        }
        if(isExplorer)kirby.drawSprite(g2);

    }


    private boolean leftClicked = false;
    private boolean rightClicked = true;
    private Point  clicked = null;


    private void updateSpritePosition(double elapsed){
        if(wHeld)spriteY +=spriteSpeedY * elapsed;
        if(aHeld)spriteX -=spriteSpeedX*elapsed;
        if(sHeld)spriteY -=spriteSpeedY * elapsed;
        if(dHeld)spriteX +=spriteSpeedY*elapsed;

        kirby.updateDirectionsHeld(wHeld, aHeld, sHeld, dHeld);
        bottomLeftX = spriteX - halfEWidth;
        bottomLeftY = spriteY - halfEHeight;
    }
    private void initializeListeners(){
        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_W) wHeld = true;
                if(e.getKeyCode() == KeyEvent.VK_A) aHeld = true;
                if(e.getKeyCode() == KeyEvent.VK_D) dHeld = true;
                if(e.getKeyCode() ==KeyEvent.VK_S) sHeld = true;
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_W) wHeld = false;
                if(e.getKeyCode() == KeyEvent.VK_A) aHeld = false;
                if(e.getKeyCode() == KeyEvent.VK_D) dHeld = false;
                if(e.getKeyCode() ==KeyEvent.VK_S) sHeld = false;
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
                    addParticle(new Particle(new Position(clicked.x, getHeight()-clicked.y), clicked.distance(mouse ), Math.toDegrees(Math.atan2((getHeight()-mouse.y)-(getHeight()-clicked.y),mouse.x-clicked.x))));
                }else if(rightClicked){
                    rightClicked = false;
                    addWall(new Wall(new Position(clicked.x, getHeight()-clicked.y), new Position(mouse.x, getHeight()-mouse.y)));
                }
                clicked = null;
            }
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
        });

    }

    public ArrayList<Particle> getParticles() {
        return particles;
    }

    public void setExplorer(boolean explorer) {
        isExplorer = explorer;
    }
    public void resetHeldKeys(){
        aHeld = false;
        wHeld = false;
        sHeld = false;
        dHeld = false;
    }
}
