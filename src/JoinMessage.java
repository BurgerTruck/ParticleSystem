import java.awt.*;
import java.io.Serializable;

public class JoinMessage extends Message implements Serializable {
    public Color joinColor;
    public JoinMessage(int clientId, Color color) {
        super(clientId, MessageType.JOIN);
        this.joinColor = color;
    }
}
