import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ReadWrite {

    public static void writeData(BufferedOutputStream writer, byte[][] data) throws IOException {

//        System.out.println("write: ");
        for (byte[] piece:data){
            byte length  = (byte)piece.length;
            writer.write(length);
//            System.out.println("length: "+length);
        }

        for (byte[] piece:data){
            writer.write(piece);
//            System.out.println("data: "+Arrays.toString(piece));
        }

    }

    public static byte[][] readData(BufferedInputStream reader, int headerLength, int expectedLength) throws IOException {
//        System.out.println("read: ");

        int[] headerData = new int[headerLength];

        int calculatedLength = headerLength;
        for (int i=0;i<headerLength;i++){
            int thing = reader.read();
//            System.out.println(thing);
            headerData[i] = thing;
            calculatedLength+=headerData[i];
//            System.out.println(i+": "+headerData[i]+"    ");
        }


//        System.out.println("expected: "+expectedLength+"    calculated: "+calculatedLength);
        if (expectedLength!=-1 && calculatedLength!=expectedLength){
            throw new RuntimeException("ERROR: UNEXPECTED FILE LENGTH");
        }

        byte[][] data = new byte[headerLength][];
        for (int i=0;i<headerLength;i++){
            int length = headerData[i];
            byte[] piece = new byte[length];
            reader.read(piece,0,length);
            data[i] = piece;
//            System.out.println(i+": "+ Arrays.toString(data[i])+"    ");
        }
        return data;
    }
}
