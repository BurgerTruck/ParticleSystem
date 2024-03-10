import java.io.Serializable;

public class Message implements Serializable {
    public enum MessageType{
        JOIN, DISCONNECT, MOVE, HEARTBEAT
    }

    public int clientId;
    public MessageType type;

    public Message(int clientId, MessageType type) {
        this.clientId = clientId;
        this.type = type;
    }

    @Override
    public String toString() {
        return "Message{" +
                "kirbyId=" + clientId +
                ", type=" + type +
                '}';
    }
}
