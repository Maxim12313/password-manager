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
import java.util.LinkedList;

public class DomainListHandler extends Communicator implements Conversation {

    DomainListHandler(BufferedInputStream in, BufferedOutputStream out, Manager manager) {
        super(in, out, manager);
    }

    @Override
    public void serverReadWrite() throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeySpecException, InvalidKeyException {
        LinkedList<String> domains = manager.registeredDomains;
        byte[][] responseData = new byte[domains.size()][];
        int i = 0;
        for (String domain:domains){
            byte[] data = domain.getBytes();
            responseData[i] = data;
            i++;
        }
        out.write((byte)'g');
        out.write((byte)domains.size()); //assume <= 255
        writeData(responseData);
        out.flush();
    }

    @Override
    public Response clientWriteRead(byte[][] data) throws IOException {
        out.write((byte)'l');
        out.flush();

        int status = in.read();
        int headerLength;
        byte[][] response;
        ;
        if ((byte)status=='b'){
            headerLength = 1;
            response = readData(headerLength);
            return new Response(new String[]{new String(response[0])});
        }
        else{
            headerLength = in.read();//assume <= 255
            response = readData(headerLength);
            String[] processedResponse = new String[headerLength];
            int i = 0;
            for (byte[] val:response){
                String domain = new String(val);
                processedResponse[i] = domain;
                i++;
            }
            return new Response(processedResponse);
        }
    }
}
