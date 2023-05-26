import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class ReadEntryHandler extends Communicator implements Conversation{
    ReadEntryHandler(BufferedInputStream in, BufferedOutputStream out, Manager manager) {
        super(in, out, manager);
    }

    @Override
    public void serverReadWrite() throws IOException {
        byte[][] data = readData(1);
        byte[] domain = data[0];

        byte[][] entryData;
        char status = 'g';
        try {
            entryData = manager.readEntryByDomain(domain);
        } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException |
                 NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | IOException |RuntimeException|
                 InvalidKeySpecException e) {
            status = 'b';
            entryData = new byte[][]{e.getMessage().getBytes()};
        }
        out.write((byte)status);
        writeData(entryData);
        out.flush();
    }

    @Override
    public Response clientWriteRead(byte[][] data) throws IOException {
        out.write((byte)'r');
        writeData(data);
        out.flush();

        int status = in.read();
        int headerLength = 3;
        byte[][] response;
        if ((byte)status=='b'){
            headerLength = 1;
            response = readData(headerLength);
            return new Response(new String(response[0]));
        }
        else {
            response = readData(headerLength);
            String[] vals = new String[headerLength];
            for (int i=0;i<headerLength;i++){
                vals[i] = new String(response[i]);
            }
            return new Response(vals);
        }
    }
}
