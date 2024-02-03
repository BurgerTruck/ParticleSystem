import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class InputField extends JPanel   {
    JLabel label;
    JTextField input;
    public InputField(String text){
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        label = new JLabel(text+": ");
        label.setFont(new Font("Monospaced", Font.ITALIC, 14));
        input = new JTextField();
//        label.setMaximumSize(new Dimension(25,50));
//        label.setMinimumSize(new Dimension(100,50));
        input.setMaximumSize(new Dimension(100,30));
        setBorder(new EmptyBorder(5, 10, 1, 10));
        add(label);
        add(input);
    }

    public String getInput(){
        return input.getText()  ;
    }
}
