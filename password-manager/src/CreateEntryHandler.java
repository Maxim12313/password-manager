import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class CreateEntryHandler extends Communicator implements Conversation {
    int clientWriteHeader = 3;
    int serverWriteHeader = 1;

    CreateEntryHandler(BufferedInputStream in, BufferedOutputStream out, Manager manager) {
        super(in, out,manager);
    }
    @Override
    public void serverReadWrite() throws IOException {
        byte[][] data = readData(clientWriteHeader);
        byte[] domain = data[0];
        byte[] username = data[1];
        byte[] password = data[2];

        byte[][] message = new byte[][]{"SUCCESSFUL: ENTRY CREATED".getBytes()};
        byte status = (byte)'g';
        try {
            manager.createNewEntry(domain,username,password);
        } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException |
                 NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException|RuntimeException | IOException e) {
            message = new byte[][]{e.getMessage().getBytes()};
            status = (byte)'b';
        }

        out.write(status);
        writeData(message);
        out.flush();
    }

    @Override
    public byte[][] clientWriteRead(byte[][] data) throws IOException {
        out.write((byte)'c');
        writeData(data);
        out.flush();

        int status = in.read();
        byte[][] response = readData(1); //always 1 header no matter what (1 for error message, 1 more success message)
        System.out.println(new String(response[0]));
        return response;
    }
}
