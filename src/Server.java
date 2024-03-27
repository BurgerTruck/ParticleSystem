import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Server{
    public static final int CLIENT_TIMEOUT = Client.HEARTBEAT_INTERVAL * 5;

    private ServerSocket serverSocket;

    private List<ClientHandler> clients;
    private int currId;

    private World serverWorld;
    private ServerController serverController;
    private GUI serverGUI;
    private LinkedList<Message> messageQueue;
    private class ServerController extends Controller{
        @Override
        public void update() throws IOException, InterruptedException {
            LinkedList<Thread> peripheryThreads = new LinkedList<>();
            synchronized (messageQueue){
                while(!messageQueue.isEmpty()){
                    Message message = messageQueue.poll();
                    switch(message.type){
                        case MOVE:
                            serverController.updateInput(message.clientId, ((MovementMessage)message).input);
                            break;
                        case REQUEST:
                            ClientHandler clientHandler = clients.get(message.clientId);
                            int id = clientHandler.client.id;
                            Kirby kirby = serverWorld.getKirby(id );
                            Position kirbyPosition = kirby.getPosition();
                            peripheryThreads.add(new Thread(() -> {
                                List<Particle> peripheryParticles = new ArrayList<>();
                                HashMap<Integer, Kirby> peripheryKirbies = new HashMap<>(   );


                                peripheryParticles =  serverWorld.getParticles().stream().parallel().filter(p -> Controller.inViewBox(kirbyPosition, p.p, Config.halfParticleWidth, Config.halfParticleHeight)).collect(Collectors.toList());
                                for(Map.Entry<Integer, Kirby> entry: serverWorld.kirbies.entrySet()){
                                    Kirby k = entry.getValue();
                                    int entryId = entry.getKey();
                                    if(Controller.inViewBox(kirbyPosition, k.getPosition(), Config.halfKirbyWidth, Config.halfKirbyHeight     )){
                                        peripheryKirbies.put(entryId, k   );
                                    }
                                }

                                World periphery = new World(peripheryParticles, peripheryKirbies    );
                                int[] maxLength = new int[1];
                                byte[][] peripheryBytes = periphery.toBytes(maxLength);
                                int numPackets = peripheryBytes.length;


                                try {
                                    long startTime = System.nanoTime();
                                    clientHandler.out.writeInt(numPackets);
                                    clientHandler.out.writeInt(maxLength[0]);
//                        System.out.println(numPackets);
                                    clientHandler.out.flush();

                                    long endTime = System.nanoTime()    ;
                                    System.out.println("TIME: "+(endTime - startTime)/1e9f);
                                } catch (SocketException e){
                                    disconnectClient(clientHandler);
                                    System.out.println("LOST CONNECTION WITH: "+id);
                                } catch (IOException e) {
                                    System.out.println(e.getMessage());
                                }
                                for(int i = 0; i < numPackets; i++){

                                    byte[] bytes = peripheryBytes[i];
                                    DatagramPacket packet = new DatagramPacket(bytes, bytes.length, clientHandler.clientAddress, clientHandler.clientUdpPort );
                                    try{
                                        clientHandler.udpSocket.send(packet);
//                                        Thread.sleep(1);
                                    }catch(Exception e){
                                        System.out.println(e.getMessage());
                                    }
                                    System.out.println(i);
                                }
                            }));
                            peripheryThreads.getLast().start();
                    }
                }
            }
            for(Thread thread: peripheryThreads){
                thread.join();
            }
            super.update();
        }

        @Override
        public boolean isExplorer() {
            return false;
        }
    }
    public Server() throws IOException {
        messageQueue = new LinkedList<>();
        serverController = new ServerController() ;
        serverWorld = new World(serverController);
        serverGUI = new GUI(serverController);

        serverController.start();

        serverSocket = new ServerSocket(6969);
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

                        Color newColor = null;
                        if(!serverWorld.getKirbies().isEmpty()){
                            newColor = Color.getHSBColor((float) Math.random(), 0.75f, 1f );
                        }
                        ClientHandler handler = new ClientHandler(newClient, clientSocket, newColor);
                        Kirby clientKirby = new Kirby(newColor) ;
                        serverWorld.addKirby(clientId, clientKirby);
                        handler.connect();
                        clients.add(handler);
                        handler.start();

//                        broadcastMessageTCP(handler, new JoinMessage(clientId, newColor));
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
        private DataOutputStream out;
        private DataInputStream in;
        private int clientUdpPort;
        private boolean isConnected;
        private DatagramSocket udpSocket;
        private InetAddress clientAddress;
        private Color clientColor;
        public ClientHandler(Client client, Socket socket, Color clientColor) throws IOException {
            this.client = client;
            this.socket = socket;
            clientAddress = socket.getInetAddress();
            this.out = new DataOutputStream(socket.getOutputStream());
            this.in = new DataInputStream(socket.getInputStream());
            isConnected = true;
            udpSocket = new DatagramSocket();
//            try {
//                udpSocket.setSendBufferSize(65000*4);
//                udpSocket.setTrafficClass(0x04);
//            } catch (SocketException e) {
//                throw new RuntimeException(e);
//            }
            this.clientColor = clientColor;
            System.out.println("ADDED CLIENT UDP SOCKET ON: "+udpSocket.getLocalSocketAddress());
            socket.setTcpNoDelay(true);
            socket.setSendBufferSize(16);
        }
        private void connect(){
            try {
                clientUdpPort = in.readInt();
                System.out.println("READ CLIENT UDP PORT: "+clientUdpPort);

                out.writeInt(client.id);
                System.out.println("NEW CLIENT: "+client.id);
//                out.writeObject(serverWorld);
                out.writeInt(udpSocket.getLocalPort());
//                out.writeObject(new JoinMessage(client.id, clientColor));
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public void run() {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true){
                        try {
                            in.readBoolean();
//                            System.out.println("RECEIVED REQUEST FROM: "+client.id);
                            synchronized (messageQueue){
                                messageQueue.add(new Message(client.id, Message.MessageType.REQUEST));
                            }
                        } catch (IOException e) {
                            disconnectClient(ClientHandler.this);
//                            throw new RuntimeException(e);
                        }
                    }

                }
            });
            thread.start();
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
//                    if (message.type == Message.MessageType.MOVE) {
//                        ((MovementMessage) message).position = serverWorld.getKirby(message.clientId).getPosition();
//                        broadcastMessageUDP(this, message);
//                    }
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
//            broadcastMessageTCP(this, new Message(client.id, Message.MessageType.DISCONNECT));

        }

    }
    private void disconnectClient(ClientHandler clientHandler){
        clientHandler.isConnected = false;
        serverWorld.removeKirby(clientHandler.client.id);
    }
    public static void main(String[] args) throws IOException {
        Server server= new Server() ;
    }
}
