import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.nio.BufferUnderflowException;
import java.util.LinkedList;
import java.util.Objects;

public class Client {
    public static final int HEARTBEAT_INTERVAL = 1000;

    int id;
    private ClientController controller  ;
    private GUI gui;
    private Socket tcpSocket;
    private DatagramSocket udpSocket;
    private int udpSendPort ;
    private LinkedList<Message> messageQueue;
    private DataOutputStream out;
    private DataInputStream in;

    public static final String serverAddress = "localhost";

    private class ClientGUI extends GUI{
        public ClientGUI(Controller controller) {
            super(controller);
        }

        @Override
        protected void initPointPanel() {

        }

        @Override
        protected void initBatchPanel() {

        }

        @Override
        protected void initExplorerButton() {

        }
    }
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
        protected double getElapsed() {
            return 0;
        }

        @Override
        public void update() throws IOException, InterruptedException {
            canvas.clearBackBuffer();
            readWorld();
//            if(controller.world!=null)controller.world.update(getElapsed());
            if(controller.world!=null){
                canvas.drawParticles();
                canvas.drawKirbies();
            }
            canvas.drawFrontBuffer();

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
        }

        @Override
        public void setExplorer(boolean isExplorer) {}
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
        gui = null;
    }

    private Client(){
        messageQueue = new LinkedList<>();
        this.controller = new ClientController();
        gui = new ClientGUI(controller);
        gui.setSize(new Dimension(GUI.canvasWidth, GUI.canvasHeight));
    }

    public void setId(int id) {
        this.id = id;
    }
    private void connectToServer() throws IOException, ClassNotFoundException, InterruptedException {
        udpSocket = new DatagramSocket();
        udpSocket.setSoTimeout(5);
        tcpSocket = new Socket(serverAddress,6969 );
        tcpSocket.setSendBufferSize(16);
        tcpSocket.setReceiveBufferSize(16);
        tcpSocket.setTcpNoDelay(true);
//        udpSocket.setReceiveBufferSize(64000*4);
//        udpSocket.setTrafficClass(0x04);
        System.out.println("CONNECTED TO SERVER ");
        out = new DataOutputStream(tcpSocket.getOutputStream());
        in = new DataInputStream(tcpSocket.getInputStream());

// get id of client
        out.writeInt(udpSocket.getLocalPort());
        out.flush();
        int id = in.readInt();
        System.out.println("CLIENT ID: "+id);

        setId(id);
        udpSendPort  = in.readInt();
        System.out.println("SERVER UDP PORT: "+udpSendPort);
        //client gets newly created kirby from server
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true) {
//                    readWorld();
//                }
//            }
//        });
//        thread.start();
        controller.start();

//        startHeartbeatThread();
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
    private void readWorld(){
        try {
            out.writeBoolean(true);
            out.flush();

            int numPackets = in.readInt();
            int length = in.readInt();
            byte[][] buffer = new byte[numPackets][length];
            for(int i = 0; i < numPackets; i++){
                DatagramPacket packet = new DatagramPacket(buffer[i], length);
                try{
                    udpSocket.receive(packet);
                }catch (SocketTimeoutException e){
                    System.out.println("PACKETS LOST: "+ (numPackets - i));
                    break;
                }

//                System.out.println(i);
            }


            World periphery = null;
            Kirby playerKirby = null;
            try{
                periphery = World.decodeBytes(buffer);
                playerKirby = periphery.getKirby(id   );
            }catch (BufferUnderflowException e){
                System.out.println(e);
            }
//                            System.out.println(periphery.getParticles());
            if (playerKirby != null) {
                    controller.setPlayerKirby(playerKirby);
                    controller.updateViewBox();
                    controller.frames++;
                    if(periphery!=null) controller.setWorld(periphery);

            }



        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
