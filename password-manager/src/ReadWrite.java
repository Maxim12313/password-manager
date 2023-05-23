import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ReadWrite {

    public static void writeData(BufferedOutputStream writer, byte[][] data) throws IOException {

        System.out.println("write: ");
//        int totalLength = 0;
//        for (byte[] piece:data){
//            byte length  = (byte)piece.length;
//            totalLength+=length;
//        }
//
//        writer.write(totalLength);
//        System.out.println("totalLength: "+totalLength);

        for (byte[] piece:data){
            byte length  = (byte)piece.length;
            writer.write(length);
            System.out.println("length: "+length);
        }

        for (byte[] piece:data){
            writer.write(piece);
            System.out.println("data: "+Arrays.toString(piece));
        }

    }

    public static byte[][] readData(BufferedInputStream reader, int headerLength) throws IOException {
        System.out.println("read: ");

        byte[] headerData = new byte[headerLength];

        for (int i=0;i<headerLength;i++){
            headerData[i] = (byte)reader.read();
            System.out.print(i+": "+headerData[i]+"    ");
        }

//        if (calculatedLength!=discoveredLength){
//            System.out.println("calLength: "+calculatedLength+"    discLength: "+discoveredLength);
//            throw new RuntimeException("ERROR: UNEXPECTED FILE LENGTH");
//        }

        byte[][] data = new byte[headerLength][];
        for (int i=0;i<headerLength;i++){
            byte length = headerData[i];
            byte[] piece = new byte[length];
            reader.read(piece,0,length);
            data[i] = piece;
            System.out.print(i+": "+ Arrays.toString(data[i])+"    ");
        }
        return data;
    }


    private static byte[] intToBytes(int num) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(num);
        return buffer.array();
    }
}
