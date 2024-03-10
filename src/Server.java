import java.awt.event.KeyEvent;
import java.io.*;
import java.net.*;
import java.util.*;

public class Server{
    public static final int CLIENT_TIMEOUT = Client.HEARTBEAT_INTERVAL * 5;

    private ServerSocket serverSocket;

    private List<ClientHandler> clients;
    private int currId;

    private World serverWorld;
    private ServerController serverController;
    private GUI serverGUI;
    private LinkedList<Message> messageQueue;
    private DatagramSocket serverUdpSocket; //only for broadcasting inputs on the server to all clients

    private class ServerController extends Controller{
        @Override
        public void update() {
            synchronized (messageQueue){
                while(!messageQueue.isEmpty()){
                    Message message = messageQueue.poll();
                    switch(message.type){
                        case MOVE:
                            serverController.updateInput(message.clientId, ((MovementMessage)message).input);

                            break;
                    }
                }
            }
            super.update();
        }

        @Override
        public void keyInput(KeyEvent e, boolean pressed) {
            boolean prevW = iswHeld();
            boolean prevA = isaHeld();
            boolean prevS = issHeld();
            boolean prevD = isdHeld();
            super.keyInput(e, pressed);


            if(iswHeld()!=prevW || isaHeld() != prevA || issHeld() !=prevS || isdHeld()!=prevD) {
                Input input = new Input(iswHeld(), isaHeld(), issHeld(), isdHeld());
                MovementMessage movementMessage = new MovementMessage(0,input, serverController.getPlayerKirby().getPosition() );
                broadcastMessageUDP(null, movementMessage);
            }
        }
    }
    public Server() throws IOException {
        messageQueue = new LinkedList<>();

        serverController = new ServerController() ;
        serverWorld = new World(serverController);
        serverGUI = new GUI(serverController);

        Kirby serverKirby = new Kirby();
        serverWorld.addKirby(currId++, serverKirby);
        serverController.setPlayerKirby(serverKirby);
        serverController.start();

        serverSocket = new ServerSocket(6969);
        serverUdpSocket = new DatagramSocket();
        clients = new ArrayList<>();

        Thread listenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("CONNECTION RECEIVED");
                        int clientId = currId++;
                        Client newClient = new Client(clientId);
                        ClientHandler handler = new ClientHandler(newClient, clientSocket  );
                        Kirby clientKirby = new Kirby() ;
                        serverWorld.addKirby(clientId, clientKirby);
                        clients.add(handler);
                        handler.start();
                        broadcastMessageTCP(handler, new Message(clientId, Message.MessageType.JOIN));
                        //server sends world with new kirby to client

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        listenThread.start();
    }
    class ClientHandler extends Thread{
        private Client client;
        private Socket socket;
        private ObjectOutputStream out;
        private ObjectInput in;
        private int clientUdpPort;
        private boolean isConnected;
        private DatagramSocket udpSocket;
        public ClientHandler(Client client, Socket socket) throws IOException {
            this.client = client;
            this.socket = socket;
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
            isConnected = true;
            udpSocket = new DatagramSocket();
        }

        @Override
        public void run() {

            try {
                clientUdpPort = in.readInt();
                System.out.println("READ CLIENT UDP PORT: "+clientUdpPort);
                out.writeInt(client.id);
                out.writeObject(serverWorld);
                out.writeInt(udpSocket.getLocalPort());
                out.flush();
                udpSocket.setSoTimeout(CLIENT_TIMEOUT);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            while(true){
                try {
                    byte[] buffer = new byte[512];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    udpSocket.receive(packet);
                    // Deserialize the received data into an object
                    ByteArrayInputStream byteStream = new ByteArrayInputStream(packet.getData());

                    ObjectInputStream objStream = new ObjectInputStream(byteStream);
                    Message message = (Message) objStream.readObject();
                    System.out.println("GOT INPUT: " + message);
                    synchronized (messageQueue) {
                        messageQueue.add(message);
                    }
                    if (message.type == Message.MessageType.MOVE) {
                        ((MovementMessage) message).position = serverWorld.getKirby(message.clientId).getPosition();
                        broadcastMessageUDP(this, message);
                    }
                }catch(SocketTimeoutException e){

                    break;
                } catch (SocketException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println("LOST CONNECTION WITH: "+client.id);
            isConnected = false;
            serverWorld.removeKirby(client.id);
            broadcastMessageTCP(this, new Message(client.id, Message.MessageType.DISCONNECT));

        }

    }
    private void broadcastMessageTCP(ClientHandler sourceClientHandler, Message message){
        try {
            for(ClientHandler clientHandler: clients){
                if(sourceClientHandler==clientHandler) continue;
                System.out.println("SENDING MESSAGE: "+message );
                if(clientHandler.isConnected){
                    clientHandler.out.writeObject(message);
                    clientHandler.out.flush();

                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void broadcastMessageUDP(ClientHandler sourceClientHandler, Message message){
        try {;
            DatagramPacket packet = MessageHelper.createUdpPacket(message);
            for(ClientHandler clientHandler: clients){
//                if(sourceClientHandler==clientHandler) continue;
                if(clientHandler.isConnected){
                    packet.setPort(clientHandler.clientUdpPort);
                    packet.setAddress(clientHandler.socket.getInetAddress());
                    if(sourceClientHandler!=null) sourceClientHandler.udpSocket.send(packet);
                    else serverUdpSocket.send(packet);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void main(String[] args) throws IOException {
        Server server= new Server() ;
    }
}
