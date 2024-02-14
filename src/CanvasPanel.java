import javax.swing.*;
import java.awt.*;
import java.awt.Point;
import java.awt.event.*;
import java.awt.image.*;
import java.util.ArrayList;
import java.util.Arrays;

public class CanvasPanel extends JPanel {
    public static final int NUM_THREADS = 8;

    private ArrayList<Particle> particles;
    private ArrayList<Wall> walls;

    private JPanel fpsPanel;
    private JLabel fpsLabel;

    private Thread[]     threads = new Thread[NUM_THREADS];
    private long prevStart = -1;
    private int frames = 0;
    private boolean playing = true;
    private boolean stepPressed = false;

    private final BufferedImage buffer;
    private final int[] pixels;
    private final int[] backPixels;
    private final  WritableRaster raster;
    private final int length = GUI.canvasWidth * GUI.canvasHeight;
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

        pixels = new int[length];
        backPixels = new int[length];
        buffer = new BufferedImage(GUI.canvasWidth, GUI.canvasHeight, BufferedImage.TYPE_BYTE_GRAY);
        Arrays.fill(backPixels, -1);
        raster = this.buffer.getRaster();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    synchronized (buffer) {
                        System.arraycopy(backPixels,0, pixels,0, length);
                        synchronized (particles){
                            if (playing || stepPressed) updateAndDrawToBuffer();
                            joinThreads();
                        }
                        raster.setPixels(0,0,GUI.canvasWidth, GUI.canvasHeight, pixels);
                    }
                    stepPressed = false;
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            repaint();
                        }
                    });
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
            for (int i = -halfHeight; i <= halfHeight; i++) {
                for (int j = -halfWidth; j <= halfWidth; j++) {
                    int newX = x1 + j;
                    int newY = y1 + i;
                    if (newX >= 0 && newX < GUI.canvasWidth && newY >= 0 && newY < GUI.canvasHeight) {
                        backPixels[newY * GUI.canvasWidth + newX] = -500;
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
    private final int halfWidth = particleWidth>>1;
    private final  int halfHeight = particleHeight>>1;
    private void drawParticle(Particle p){
        drawPixel(p.p);
    }
    private boolean drawPixel(Position p ){
        int x = (int)(p.x);
        int y = (int)(getHeight() - p.y);

        int index = y * GUI.canvasWidth + x;
        if(index <= 0 || index>=length) return false;
        if(pixels[index]!=-1) return false;

        index = index- GUI.canvasWidth * halfHeight - halfWidth;
        for (int i = 0; i < particleHeight; i++, index += GUI.canvasWidth) {
            int nextIndex = index;
            for (int j = 0; j < particleWidth; j++, nextIndex++) {

                if(nextIndex<0 || nextIndex>=length) continue;
                pixels[nextIndex] = -500;

            }

        }

        return true;
    }


    private void toggleTimer(){
        playing = !playing;
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
    private void updateAndDrawToBuffer(){
        long curr = System.nanoTime();
        double elapsed = prevStart == -1 || stepPressed ? (1d / 144d) : (curr - prevStart) / 1000000000d;
        prevStart = curr;
        for (int i = 0; i < NUM_THREADS; i++) {
            if(i >=particles.size()) break;
            int finalI = i;
            threads[i] = new Thread(() -> {
                for (int j = finalI; j < particles.size(); j += NUM_THREADS) {
                    particles.get(j).move(walls, elapsed);
                    drawParticle( particles.get(j));
                }
            });
            threads[i].start();
        }
    }
    private BasicStroke stroke = new BasicStroke(3);

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        synchronized (buffer){
            g2.drawImage(buffer, null, 0,0);
//            g2.drawImage(buffer, 0,0, null  );
//            g2.drawImage(buffer, 0,0, null  );
        }
        g2.setStroke(stroke);
        if(clicked!=null){
            Point mouse = MouseInfo.getPointerInfo().getLocation();
            SwingUtilities.convertPointFromScreen(mouse, this);
            g2.drawLine(clicked.x, clicked.y, mouse.x, mouse.y);
        }
        frames++;
    }


    private boolean leftClicked = false;
    private boolean rightClicked = true;
    private Point  clicked = null;

    private void initializeListeners(){

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
//                System.out.println("RELEASEd");
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


        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_SPACE){
                    toggleTimer();
                    prevStart = System.nanoTime();
                }
                else if(e.getKeyCode()==KeyEvent.VK_RIGHT){
                    stepPressed = true;

                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });
    }

    public ArrayList<Particle> getParticles() {
        return particles;
    }
}
