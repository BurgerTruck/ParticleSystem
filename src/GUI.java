import javax.swing.*;
import java.awt.*;


public class GUI extends JFrame {
    public static int canvasWidth = 1280;
    public static int canvasHeight = 720;

    private JPanel addPointPanel;
    private JPanel addWallPanel;
    private CanvasPanel canvas;

    public GUI(){
        this.setSize((int) (1.25*canvasWidth), (int) (1.1*canvasHeight));
        this.setVisible(true);
        this.setDefaultCloseOperation(3);

        canvas = new CanvasPanel(canvasWidth, canvasHeight);
        initPointPanel();
        initWallPanel();

        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        addPointPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        addWallPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidePanel.add(addPointPanel);
        sidePanel.add(Box.createVerticalStrut(20));
        sidePanel.add(addWallPanel);
        sidePanel.setBorder(BorderFactory.createLineBorder(Color.RED));


        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));

        mainPanel.add(canvas);
        mainPanel.add(sidePanel   );
        add(mainPanel);
        this.repaint();
        this.revalidate();
    }
    public static void main(String[] args) {
        System.out.println("Hello world!");
        GUI gui = new GUI();
    }
    private void initPointPanel(){
        addPointPanel = new JPanel();
        addPointPanel.setLayout(new BoxLayout(addPointPanel, BoxLayout.Y_AXIS));

        JPanel coordinatesPanel = new JPanel();
        coordinatesPanel.setLayout(new BoxLayout(coordinatesPanel, BoxLayout.X_AXIS));

        JPanel velocityPanel = new JPanel();
        velocityPanel.setLayout(new BoxLayout(velocityPanel, BoxLayout.X_AXIS));

        InputField xField = new InputField("X");
        InputField yField = new InputField("Y");
        coordinatesPanel.add(xField);
        coordinatesPanel.add(yField);

        InputField speedField = new InputField("Speed");
        InputField angleField = new InputField("Angle");
        velocityPanel.add(speedField);
        velocityPanel.add(angleField);

        JButton confirmButton = new JButton("Add Point");

        addPointPanel.add(coordinatesPanel);
        addPointPanel.add(velocityPanel);
        addPointPanel.add(confirmButton);

//        addPointPanel.setBorder(BorderFactory.createLineBorder(Color.RED));

        coordinatesPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        velocityPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        confirmButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        confirmButton.addActionListener(e -> {
            double  x = Double.parseDouble(xField.getInput());
            double y = Double.parseDouble(yField.getInput());
            double speed = Double.parseDouble(speedField.getInput());
            double angle = Double.parseDouble(angleField.getInput());
            Particle particle = new Particle(new Position(x,y), speed, angle);
            canvas.addParticle(particle);
//            System.out.println("ADDED PARTICLE?");
        });
    }

    private void initWallPanel(){
        addWallPanel = new JPanel();
        addWallPanel.setLayout(new BoxLayout(addWallPanel, BoxLayout.Y_AXIS));

        JPanel coordinatesPanel1 = new JPanel();
        coordinatesPanel1.setLayout(new BoxLayout(coordinatesPanel1, BoxLayout.X_AXIS));

        JPanel coordinatesPanel2 = new JPanel();
        coordinatesPanel2.setLayout(new BoxLayout(coordinatesPanel2, BoxLayout.X_AXIS));

        InputField x1Field = new InputField("X1");
        InputField y1Field = new InputField("Y1");

        InputField x2Field = new InputField("X2");
        InputField y2Field = new InputField("Y2");

        coordinatesPanel1.add(x1Field);
        coordinatesPanel1.add(y1Field)   ;

        coordinatesPanel2.add(x2Field)   ;
        coordinatesPanel2.add(y2Field);

        JButton confirm = new JButton("Add Wall");

        addWallPanel.add(coordinatesPanel1);
        addWallPanel.add(coordinatesPanel2);
        addWallPanel.add(confirm);

        coordinatesPanel1.setAlignmentX(Component.CENTER_ALIGNMENT);
        coordinatesPanel2.setAlignmentX(Component.CENTER_ALIGNMENT);
        confirm.setAlignmentX(Component.CENTER_ALIGNMENT);

        confirm.addActionListener(e -> {
            double  x1 = Double.parseDouble(x1Field.getInput());
            double y1 = Double.parseDouble(y1Field.getInput());

            double  x2 = Double.parseDouble(x2Field.getInput());
            double y2 = Double.parseDouble(y2Field.getInput());

            canvas.addWall(new Wall(new Position(x1,y1), new Position(x2,y2)));
        });
    }
}
