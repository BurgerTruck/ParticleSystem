import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class MessageHelper {

    public static DatagramPacket createUdpPacket(Object obj, InetAddress destination, int udpPort) throws IOException {
        DatagramPacket packet = createUdpPacket(obj);
        packet.setAddress(destination);
        packet.setPort(udpPort);
        return packet;
    }
    public static DatagramPacket createUdpPacket(Object object) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
        objStream.writeObject(object);
        byte[] data = byteStream.toByteArray();
//        System.out.println("CREATED MESSAGE WITH LENGTH: "+data.length);
        DatagramPacket packet = new DatagramPacket(data,0, data.length);
        return packet;
    }
}
