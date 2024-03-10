public class HeartbeatMessage extends Message{
    public HeartbeatMessage(int clientId) {
        super(clientId, MessageType.HEARTBEAT);
    }
}
