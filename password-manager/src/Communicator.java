import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

public abstract class Communicator {

    BufferedInputStream in;
    BufferedOutputStream out;
    Manager manager;
    Communicator(BufferedInputStream in, BufferedOutputStream out,Manager manager) {
        this.in = in;
        this.out = out;
        this.manager = manager;
    }

    Communicator(BufferedInputStream in, BufferedOutputStream out){
        this.in = in;
        this.out = out;
        this.manager = null;
    }
    public byte[][] readData(int headerLength) throws IOException {
        return ReadWrite.readData(in,headerLength,-1);
    }

    public void writeData(byte[][] data) throws IOException {
        ReadWrite.writeData(out,data);
    }
}
