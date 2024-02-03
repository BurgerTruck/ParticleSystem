import javax.swing.*;
import java.awt.*;
import java.awt.Point;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class CanvasPanel extends JPanel implements ActionListener {
    public static final int NUM_THREADS = 16;

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

        timer = new Timer(1, this);
        timer.start();
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
    private void drawPoint(Graphics2D g, Particle particle){
        int x = (int) Math.round(particle.p.x );
        int y = (int) (getHeight()- Math.round(particle.p.y));

        g.drawLine(x,y,x,y);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
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
    private BasicStroke stroke = new BasicStroke(3);
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(stroke);

        long curr = System.nanoTime();
        double elapsed = prevStart == -1 || stepPressed ? (1d / 144d) : (curr - prevStart) / 1000000000d;
//        System.out.println(elapsed);
        prevStart = curr;
        for (int i = 0; i < NUM_THREADS; i++) {
            int finalI = i;
            threads[i] = new Thread(() -> {
                for (int j = finalI; j < particles.size(); j += NUM_THREADS) {
                    if(playing || stepPressed)particles.get(j).move(walls, elapsed);
                    drawPoint(g2, particles.get(j));
                }
                for(int j = finalI;j<walls.size();j+=NUM_THREADS){
                    drawWall(g2, walls.get(j));
                }
            });
            threads[i].start();
        }
        stepPressed = false;
        waitThreads();
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
                System.out.println("KEY PRESSED");
                if(e.getKeyCode()==KeyEvent.VK_SPACE){
                    toggleTimer();
                    System.out.println("SPACE PRESED");
                    prevStart = System.nanoTime();
                }
                else if(e.getKeyCode()==KeyEvent.VK_RIGHT){
                    stepPressed = true;
                    actionPerformed(null);
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });
    }


}
