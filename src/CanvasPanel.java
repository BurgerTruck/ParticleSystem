import javax.swing.*;
import java.awt.*;
import java.awt.Point;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.TimerTask;

public class CanvasPanel extends JPanel {
    public static final int NUM_THREADS = 9;
    public static final int SQRT_THREADS = (int) Math.sqrt(NUM_THREADS);
    private static final int BUFFER_WIDTH = GUI.canvasWidth/SQRT_THREADS ;
    private static final int BUFFER_HEIGHT = GUI.canvasHeight/SQRT_THREADS;
    private static final int BUFFER_PADDING = 0;

    private ArrayList<Particle> particles;
    private ArrayList<Wall> walls;

    private JPanel fpsPanel;
    private JLabel fpsLabel;

    private Thread[]     threads = new Thread[NUM_THREADS];
    private long prevStart = -1;
    private int frames = 0;
    private boolean playing = true;
    private boolean stepPressed = false;
    private Timer timer;
    CanvasBuffer[] buffers;
    //    private LinkedList<Particle>[] particlesToUpdate;
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
//        particlesToUpdate = new LinkedList[NUM_THREADS];
//        for(int i = 0; i < particlesToUpdate.length; i++){
//            particlesToUpdate[i]    =  new LinkedList();
//        }
        particles = new ArrayList<>();
        walls = new ArrayList<>();
        buffers = new CanvasBuffer[NUM_THREADS];
        for(int i = 0; i < SQRT_THREADS; i++){
            for(int j =0 ; j < SQRT_THREADS; j++){
                buffers[i*SQRT_THREADS + j] = new CanvasBuffer(BUFFER_WIDTH+2*BUFFER_PADDING, BUFFER_HEIGHT + 2* BUFFER_PADDING, j * BUFFER_WIDTH - BUFFER_PADDING, i * BUFFER_HEIGHT - BUFFER_PADDING);
//                System.out.println(buffers[i*SQRT_THREADS+j].startY);
//                System.out.println(buffers[i*SQRT_THREADS+j].endY   );
            }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (playing || stepPressed) updateParticles();
                    waitThreads();
                    synchronized (buffers) {
                        for (int i = 0; i < threads.length; i++) {
                            int finalI = i;
                            threads[i] = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    CanvasBuffer buffer = buffers[finalI];

                                    buffer.g2d.clearRect(0, 0, buffer.getWidth(), buffer.getHeight());
                                    buffer.g2d.setColor(Color.BLACK);
                                    for (int j = 0; j < particles.size(); j++) {
                                        Particle p = particles.get(j);
                                        if (p.p.x >= buffer.startX && p.p.x <= buffer.endX && p.p.y >= buffer.startY && p.p.y <= buffer.endY) {
                                            drawPoint(buffer.g2d, p);
                                        }

                                    }
                                }
                            });
                            threads[i].start();
                        }
                        stepPressed = false;
                        waitThreads();
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            repaint();
                        }
                    });
                }
            }
        }).start();

//        new Timer(1, new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                repaint();
//            }
//        }).start();
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

    public void addWall(Wall wall){
        walls.add(wall);
    }
    private void drawWall(Graphics2D g, Wall wall){
        g.drawLine((int) wall.p1.x, (int) (getHeight()- wall.p1.y), (int) wall.p2.x, (int) (getHeight()-wall.p2.y));
    }
    private int particleWidth =3;
    private int particleHeight = 3;
    private int halfWidth = particleWidth>>1;
    private int halfHeight = particleHeight>>1;
    private void drawPoint(Graphics2D g, Particle particle){

        int x = (int) (particle.p.x );
        int y = (int) (particle.p.y);
//
//        g.fillRect(1,199,3,3);
//        g.drawLine(x,y,x,y);
//        g.fillRect((x-halfWidth)%BUFFER_WIDTH + BUFFER_PADDING , (y-halfHeight)%BUFFER_HEIGHT, particleWidth,particleHeight);
//        System.out.println(x%BUFFER_WIDTH+halfWidth);
//        System.out.println(BUFFER_HEIGHT- y%BUFFER_HEIGHT-halfHeight);
//        System.out.println();
        g.fillRect(x%BUFFER_WIDTH-halfWidth, BUFFER_HEIGHT-1- y%BUFFER_HEIGHT-halfHeight , particleWidth,particleHeight);

    }


    private void toggleTimer(){
        playing = !playing;
    }
    private void waitThreads(){
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private void updateParticles(){
        long curr = System.nanoTime();
        double elapsed = prevStart == -1 || stepPressed ? (1d / 144d) : (curr - prevStart) / 1000000000d;
//        double elapsed = prevStart == -1 || stepPressed ? (1d / 144d) : (curr - prevStart) / 1000000000d;
//        double elapsed = 1d/144d;
        prevStart = curr;
        for (int i = 0; i < NUM_THREADS; i++) {
            int finalI = i;
            threads[i] = new Thread(() -> {
                for(int j = finalI; j < particles.size(); j+=NUM_THREADS){
                    particles.get(j).move(walls, elapsed);
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
        synchronized (buffers) {
            for (CanvasBuffer buffer : buffers) {
                    g2.drawImage(buffer, null, buffer.startX, GUI.canvasHeight - buffer.startY - BUFFER_HEIGHT - BUFFER_PADDING);

            }
        }
        g2.setStroke(stroke);
        for(Wall wall: walls)drawWall(g2, wall);
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


}
