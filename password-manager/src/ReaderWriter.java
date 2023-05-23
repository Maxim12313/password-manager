import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public abstract class ReaderWriter {

    public void writeData(BufferedOutputStream writer, byte[][] data) throws IOException {
        System.out.println("write: ");
        int totalLength = 0;
        int num = 0;
        for (byte[] piece:data){
            byte length  = (byte)piece.length;
            writer.write(length);
            System.out.print(num+": "+length+"    ");
            num++;
            totalLength++;
        }

        num= 0;
        for (byte[] piece:data){
            writer.write(piece);
            System.out.print(num+": "+ Arrays.toString(piece)+"    ");
            num++;
            totalLength+=piece.length;
        }

        System.out.println("");

        num = 0;
        for (byte[] piece:data){
            writer.write(piece);
            System.out.print(num+": "+piece.length+"    ");
            num++;

        }
        System.out.println("tracked length: "+totalLength);
        writer.close();
    }

    public byte[][] readData(BufferedInputStream reader, int headerLength, int expectedLength) throws IOException {
        System.out.println("read: ");

        int totalLength = headerLength;
        byte[] headerData = new byte[headerLength];
        for (int i=0;i<headerLength;i++){
            headerData[i] = (byte)reader.read();
            totalLength+=headerData[i];
            System.out.print(i+": "+headerData[i]+"    ");
        }

//        if (totalLength!=expectedLength){
//            System.out.println("expectedLengh: "+expectedLength+"    actual: "+totalLength);
//            throw new RuntimeException("ERROR: UNEXPECTED FILE LENGTH");
//        }

        byte[][] data = new byte[headerLength][];
        for (int i=0;i<headerLength;i++){
            byte length = headerData[i];
            byte[] piece = new byte[length];
            reader.read(piece,0,length);
            data[i] = piece;
            System.out.print(i+": "+Arrays.toString(data[i])+"    ");
        }
        reader.close();

        return data;
    }


}
