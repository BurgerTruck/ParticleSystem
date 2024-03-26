import java.io.Serializable;
import java.nio.ByteBuffer;

public class Position implements Serializable
{
    double x;
    double y;

    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    public byte[] toBytes(){
        ByteBuffer buffer = ByteBuffer.allocate(numBytes());
        buffer.putDouble(x);
        buffer.putDouble(y);
        return buffer.array();
    }


    public static int numBytes(){
        return 2*Double.BYTES;
    }

    public static Position decodeBytes(byte[] bytes){
        ByteBuffer buffer = ByteBuffer.wrap(bytes   );
        double x= buffer.getDouble();
        double y = buffer.getDouble();
        return new Position(x, y);
    }
}
