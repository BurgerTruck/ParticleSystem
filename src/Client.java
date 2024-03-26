import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Objects;

public class Client {
    public static final int HEARTBEAT_INTERVAL = 1000;

    int id;
    private ClientController controller  ;
    private World world;
    private GUI gui;
    private Socket tcpSocket;
    private DatagramSocket udpSocket;
    private int udpSendPort ;
    private LinkedList<Message> messageQueue;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public static final String serverAddress = "localhost";
    private class ClientController extends Controller{
        @Override
        public void keyInput(KeyEvent e, boolean pressed) {
            boolean prevW = iswHeld();
            boolean prevA = isaHeld();
            boolean prevS = issHeld();
            boolean prevD = isdHeld();
            super.keyInput(e, pressed);
            if(iswHeld()!=prevW || isaHeld() != prevA || issHeld() !=prevS || isdHeld()!=prevD){
                try {
                    sendInput();

                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        @Override
        public void update() throws IOException {
            canvas.clearBackBuffer();
            canvas.drawFrontBuffer();
//            world.update();
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        canvas.repaint();
                    }
                });
            } catch (InterruptedException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            frames++;
        }

        @Override
        public boolean isExplorer() {
            return true;
        }
//        @Override
//        protected void updateViewBox(){
//
//        }
    }

    public Client(int id) {
        this.id = id;
        controller = null;
        world = null;
        gui = null;
    }

    private Client(){
        messageQueue = new LinkedList<>();
        this.controller = new ClientController();
        gui = new GUI(controller);


    }

    public void setId(int id) {
        this.id = id;
    }
    private void connectToServer() throws IOException, ClassNotFoundException, InterruptedException {
        udpSocket = new DatagramSocket();
        tcpSocket = new Socket(serverAddress,6969 );
        System.out.println("CONNECTED TO SERVER ");
        out = new ObjectOutputStream(tcpSocket.getOutputStream());
        in = new ObjectInputStream(tcpSocket.getInputStream());

// get id of client
        out.writeInt(udpSocket.getLocalPort());
        out.flush();
        int id = in.readInt();
        System.out.println("CLIENT ID: "+id);
        setId(id);
        udpSendPort  = in.readInt();
        System.out.println("SERVER UDP PORT: "+udpSendPort);
        //client gets newly created kirby from server
        controller.start();

        startUdpListeningThread();
        startHeartbeatThread();
        startTcpListeningThread();
    }
    private void startHeartbeatThread(){
        Thread thread=  new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    DatagramPacket packet;
                    try {
                        packet = MessageHelper.createUdpPacket(new HeartbeatMessage(id), tcpSocket.getInetAddress(), udpSendPort );
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        udpSocket.send(packet);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        Thread.sleep(HEARTBEAT_INTERVAL);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        });
        thread.start();
    }
    private void startTcpListeningThread(){
        Thread thread= new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try{
                        Message message = (Message)in.readObject();
                        synchronized (messageQueue){
                            messageQueue.add(message);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        thread.start();

    }
    private void startUdpListeningThread(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[16384];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                while(true){
                    try {
                        udpSocket.receive(packet);
                        ByteArrayInputStream byteStream = new ByteArrayInputStream(packet.getData());
                        ObjectInputStream objStream = new ObjectInputStream(byteStream);
                        World periphery = (World) objStream.readObject();
                        controller.setWorld(periphery);
                        for(Kirby kirby:periphery.getKirbies()){
                            kirby.initializeColor();
                        }
                        Kirby playerKirby = periphery.getKirby(id   );
                        controller.setPlayerKirby(playerKirby);
                        controller.updateViewBox();
//                        System.out.println(periphery.kirbies);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }

                    // Deserialize the received data into an object

                }
            }
        });
        thread.start();
    }
    private void sendInput() throws IOException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Input input=  new Input(controller.iswHeld(), controller.isaHeld(),controller.issHeld(),controller.isdHeld() );
                    DatagramPacket packet = MessageHelper.createUdpPacket(new MovementMessage(id, input),
                             tcpSocket.getInetAddress(), udpSendPort);
                    udpSocket.send(packet);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        });
        thread.start();

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return id == client.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        Client client = new Client();
        client.connectToServer();


    }


}
