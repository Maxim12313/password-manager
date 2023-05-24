import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public interface Conversation{

    void serverReadWrite() throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeySpecException, InvalidKeyException;

    byte[][] clientWriteRead(byte[][] data) throws IOException;

    //define message, how to read, get request type, funcion from manager to response, response to bytes
}
