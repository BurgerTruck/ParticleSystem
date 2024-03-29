import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;


public class GUI extends JFrame {
    public static int canvasWidth = 1280;
    public static int canvasHeight = 720;

    private JPanel addPointPanel;
    private JPanel addWallPanel;
    private JPanel addBatchPanel;
    private CanvasPanel canvas;

    public GUI(){
        this.setSize((int) (1.25*canvasWidth), (int) (1.1*canvasHeight));
        this.setVisible(true);
        this.setDefaultCloseOperation(3);

        canvas = new CanvasPanel(canvasWidth, canvasHeight);
        initPointPanel();
        initWallPanel();
        initBatchPanel();

        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        addPointPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        addWallPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidePanel.add(addPointPanel);
        sidePanel.add(Box.createVerticalStrut(20));
        sidePanel.add(addWallPanel);
        sidePanel.add(Box.createVerticalStrut(20));
        sidePanel.add(addBatchPanel);


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

        InputPanel coordinatesPanel = new InputPanel();
        InputPanel velocityPanel = new InputPanel();

        InputField xField = coordinatesPanel.addInput(new InputField("X")) ;
        InputField yField = coordinatesPanel.addInput(new InputField("Y")) ;

        InputField speedField = velocityPanel.addInput(new InputField("v")) ;
        InputField angleField = velocityPanel.addInput(new InputField("θ")) ;

        JButton confirmButton = new JButton("Confirm");

        addPointPanel.add(coordinatesPanel);
        addPointPanel.add(velocityPanel);
        addPointPanel.add(confirmButton);
        confirmButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addPointPanel.add(Box.createVerticalStrut(5));
        confirmButton.addActionListener(e -> {
            double  x = Double.parseDouble(xField.getInput());
            double y = Double.parseDouble(yField.getInput());
            double speed = Double.parseDouble(speedField.getInput());
            double angle = Double.parseDouble(angleField.getInput());
            Particle particle = new Particle(new Position(x,y), speed, angle);
            canvas.addParticle(particle);
        });
        addPointPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), "Add Particle"));


    }

    private void initWallPanel(){
        addWallPanel = new JPanel();
        addWallPanel.setLayout(new BoxLayout(addWallPanel, BoxLayout.Y_AXIS));

        InputPanel coordinatesPanel1 = new InputPanel();
        InputPanel coordinatesPanel2 = new InputPanel();

        InputField x1Field = coordinatesPanel1.addInput(new InputField("X₁"));
        InputField y1Field = coordinatesPanel1.addInput(new InputField("Y₁"));

        InputField x2Field = coordinatesPanel2.addInput(new InputField("X₂"));
        InputField y2Field = coordinatesPanel2.addInput(new InputField("Y₂"));

        JButton confirm = new JButton("Confirm");

        addWallPanel.add(coordinatesPanel1);
        addWallPanel.add(coordinatesPanel2);
        addWallPanel.add(confirm);
        addWallPanel.add(Box.createVerticalStrut(5));
        confirm.setAlignmentX(Component.CENTER_ALIGNMENT);

        confirm.addActionListener(e -> {
            double  x1 = Double.parseDouble(x1Field.getInput());
            double y1 = Double.parseDouble(y1Field.getInput());

            double  x2 = Double.parseDouble(x2Field.getInput());
            double y2 = Double.parseDouble(y2Field.getInput());

            canvas.addWall(new Wall(new Position(x1,y1), new Position(x2,y2)));
        });
        addWallPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), "Add Wall"));

    }

    private void initBatchPanel(){
        addBatchPanel = new JPanel();
        addBatchPanel.setLayout(new BoxLayout(addBatchPanel, BoxLayout.Y_AXIS));

        InputPanel coordinatesPanel1 = new InputPanel();
        InputPanel coordinatesPanel2 = new InputPanel();

        InputField x1Field = coordinatesPanel1.addInput(new InputField("X₁"));
        InputField y1Field = coordinatesPanel1.addInput(new InputField("Y₁"));

        InputField x2Field = coordinatesPanel2.addInput(new InputField("X₂"));
        InputField y2Field = coordinatesPanel2.addInput(new InputField("Y₂"));

        InputPanel speedPanel = new InputPanel();
        InputField startSpeed = speedPanel.addInput(new InputField("v₁"));
        InputField endSPeed = speedPanel.addInput(new InputField("v₂"));

        InputPanel anglePanel = new InputPanel();
        InputField startAngle = anglePanel.addInput(new InputField("θ₁"));
        InputField endAngle = anglePanel.addInput(new InputField("θ₂"));

        InputField numPoints = new InputField("N");

        JButton confirm = new JButton("Confirm");

        addBatchPanel.add(coordinatesPanel1);
        addBatchPanel.add(coordinatesPanel2);
        addBatchPanel.add(speedPanel);
        addBatchPanel.add(anglePanel);
        addBatchPanel.add(numPoints);
        addBatchPanel.add(Box.createVerticalStrut(5));
        addBatchPanel.add(confirm);
        addBatchPanel.add(Box.createVerticalStrut(5));
        numPoints.setAlignmentX(Component.CENTER_ALIGNMENT);
        confirm.setAlignmentX(Component.CENTER_ALIGNMENT);

        confirm.addActionListener(e -> {
            double  x1 = Double.parseDouble(x1Field.getInput());
            double y1 = Double.parseDouble(y1Field.getInput());

            double  x2 = Double.parseDouble(x2Field.getInput());
            double y2 = Double.parseDouble(y2Field.getInput());

            double v1 = Double.parseDouble(startSpeed.getInput());
            double v2 = Double.parseDouble(endSPeed.getInput());

            double angle1 = Double.parseDouble(startAngle.getInput());
            double angle2 = Double.parseDouble(endAngle.getInput());

            int n = Integer.parseInt(numPoints.getInput());

            double xStep = (x2-x1)/n;
            double yStep = (y2-y1)/n;
            double vStep = (v2-v1)/n;
            double angleStep = (angle2-angle1)/n;
            for(int i = 0; i < n; i++){
                Particle particle = new Particle(new Position(x1, y1), v1, angle1);
                x1+=xStep; y1+=yStep; v1+=vStep; angle1+=angleStep;
                canvas.addParticle(particle);
            }
        });
        addBatchPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), "Add Batch"));
    }
}
