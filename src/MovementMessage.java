import java.io.Serializable;

public class MovementMessage extends Message implements Serializable {
    public Input input;
    public Position position;
    public MovementMessage(int kirbyId, Input input, Position position) {
        super(kirbyId, MessageType.MOVE);
        this.input = input;
        this.position = position;
    }

    @Override
    public String toString() {
        return "MovementMessage{" +
                "input=" + input +
                ", kirbyId=" + clientId +
                '}';
    }
}
