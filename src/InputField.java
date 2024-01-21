import javax.swing.*;
import java.awt.*;

public class InputField extends JPanel   {
    JLabel label;
    JTextField input;
    public InputField(String text){
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        label = new JLabel(text +": ");
        input = new JTextField();
        input.setMaximumSize(new Dimension(100,50));
        add(label);
        add(input);
    }

    public String getInput(){
        return input.getText()  ;
    }
}
