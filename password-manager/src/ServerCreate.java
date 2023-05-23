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

public class ServerCreate extends ReaderWriter implements Conversation{
    @Override
    public byte[] read(BufferedInputStream in, Manager manager) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeySpecException, InvalidKeyException {
        byte[][] data = readData(in,1,in.available());
        byte[][] send = manager.readEntryByDomain(data[0]);
        return null;
    }

    @Override
    public byte[] write(BufferedOutputStream out,Manager manager) {
        return new byte[0];
    }
}
