import java.io.Serializable;

public class MovementMessage extends Message implements Serializable {
    public Input input;
    public MovementMessage(int kirbyId, Input input) {
        super(kirbyId, MessageType.MOVE);
        this.input = input;
    }

    @Override
    public String toString() {
        return "MovementMessage{" +
                "input=" + input +
                ", kirbyId=" + clientId +
                '}';
    }
}
