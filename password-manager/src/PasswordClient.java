import java.io.*;
import java.net.*;

public class PasswordClient{
    private Socket clientSocket;
    public BufferedOutputStream out;
    public BufferedInputStream in;

    public void startConnection(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        out = new BufferedOutputStream(clientSocket.getOutputStream());
        in = new BufferedInputStream(clientSocket.getInputStream());
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }
}