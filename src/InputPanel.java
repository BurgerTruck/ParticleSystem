import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class InputPanel extends JPanel {
    public InputPanel() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setAlignmentX(Component.CENTER_ALIGNMENT);
//        setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED)));
;
//        setBorder(new EmptyBorder(10, 10, 10, 10));
    }

    public InputField addInput(InputField field){

        add(field);
        return field;
    }

}
