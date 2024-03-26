import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

public class Server{
    public static final int CLIENT_TIMEOUT = Client.HEARTBEAT_INTERVAL * 5;

    private ServerSocket serverSocket;

    private List<ClientHandler> clients;
    private int currId;

    private World serverWorld;
    private ServerController serverController;
    private GUI serverGUI;
    private LinkedList<Message> messageQueue;
    private Integer numConnected = 0;
    private class ServerController extends Controller{
        @Override
        public void update() throws IOException, InterruptedException {
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

            Integer numClients = 0;
            Integer size;
            synchronized (numClients){
                numClients = numConnected;
                size = clients.size();
            }
            Thread[] threads = new Thread[numClients];
            int clientIndex = 0;
            for(int i = 0; i < clients.size(); i++){
                ClientHandler clientHandler = clients.get(i);
                if(!clientHandler.isConnected) continue;
                int id = clientHandler.client.id;
                Kirby kirby = serverWorld.getKirby(id );
                if(kirby==null)continue;
                threads[clientIndex] = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Position kirbyPosition = kirby.getPosition();
                        ArrayList<Particle> peripheryParticles = new ArrayList<>(   );
                        HashMap<Integer, Kirby> peripheryKirbies = new HashMap<>(   );

                        for(int j = 0; j < world.getParticles().size(); j++){
                            Particle p = world.getParticles().get(j);
                            if(Controller.inViewBox(kirbyPosition, p.p, Config.halfParticleWidth, Config.halfParticleHeight     )){
//                        System.out.println(p.p);
//                        System.out.println(kirbyPosition);
//                        System.out.println();
                                peripheryParticles.add(p    );
                            }
                        }
                        for(Map.Entry<Integer, Kirby> entry: world.kirbies.entrySet()){
                            Kirby k = entry.getValue();
                            int entryId = entry.getKey();
                            if(Controller.inViewBox(kirbyPosition, k.getPosition(), Config.halfKirbyWidth, Config.halfKirbyHeight     )){
                                peripheryKirbies.put(entryId, k   );
                            }
                        }
                        World periphery = new World(peripheryParticles, peripheryKirbies    );
//                DatagramPacket packet = MessageHelper.createUdpPacket(periphery, clientHandler.clientAddress, clientHandler.clientUdpPort);
                        byte[] peripheryBytes = periphery.toBytes();
                        DatagramPacket packet = new DatagramPacket(peripheryBytes, peripheryBytes.length, clientHandler.clientAddress, clientHandler.clientUdpPort );
                        try{
                            clientHandler.udpSocket.send(packet);
                        }catch(Exception e){
                            System.out.println(e.getMessage());
                        }
                    }
                });
                threads[clientIndex].start();
                for(Thread thread: threads){
                    if(thread == null) continue;
                    thread.join();
                }
//                }
            }
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
                        clients.add(handler);
                        handler.start();
                        synchronized (numConnected){
                            numConnected++;
                        }
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
        private ObjectOutputStream out;
        private ObjectInput in;
        private int clientUdpPort;
        private boolean isConnected;
        private DatagramSocket udpSocket;
        private InetAddress clientAddress;
        private Color clientColor;
        public ClientHandler(Client client, Socket socket, Color clientColor) throws IOException {
            this.client = client;
            this.socket = socket;
            clientAddress = socket.getInetAddress();
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
            isConnected = true;
            udpSocket = new DatagramSocket();
            this.clientColor = clientColor;
            System.out.println("ADDED CLIENT UDP SOCKET ON: "+udpSocket.getLocalSocketAddress());
        }

        @Override
        public void run() {

            try {
                clientUdpPort = in.readInt();
                System.out.println("READ CLIENT UDP PORT: "+clientUdpPort);
                out.writeInt(client.id);
//                out.writeObject(serverWorld);
                out.writeInt(udpSocket.getLocalPort());
//                out.writeObject(new JoinMessage(client.id, clientColor));
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
            System.out.println("LOST CONNECTION WITH: "+client.id);
            isConnected = false;
            serverWorld.removeKirby(client.id);

            synchronized (numConnected){
                numConnected--;
            }


//            broadcastMessageTCP(this, new Message(client.id, Message.MessageType.DISCONNECT));

        }

    }
    public static void main(String[] args) throws IOException {
        Server server= new Server() ;
    }
}
