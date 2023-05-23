import java.io.*;
import java.net.*;

public class PasswordClient extends ReaderWriter {
    private Socket clientSocket;
    private BufferedOutputStream out;
    private BufferedInputStream in;

    public void startConnection(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        out = new BufferedOutputStream(clientSocket.getOutputStream());
        in = new BufferedInputStream(clientSocket.getInputStream());
    }

    public byte[][] sendData(byte[][] data,byte protocol) throws IOException {
        out.write(protocol);
        writeData(out,data);
        return null;
    }
    public String sendMessage(String msg) throws IOException {
//        out.println(msg);
//        String resp = in.readLine();
//        return resp;
        return null;
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }


    public static void main(String[] args) throws IOException {
        PasswordClient client = new PasswordClient();
        client.startConnection("127.0.0.1", 50501);
        String resp1 = client.sendMessage("hello");
        System.out.println(resp1);
        String resp2 = client.sendMessage("world");
        System.out.println(resp2);
        String resp3 = client.sendMessage("!");
        String resp4 = client.sendMessage(".");
        client.stopConnection();

        System.out.println("resp1: "+resp1+"    resp2: "+resp2+"    resp3: "+resp3+"    resp4: "+resp4);

    }


}