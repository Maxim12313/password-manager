import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class GUI {

    public static void main(String[] args) throws IOException {
        new GUI();
    }

    JFrame myJFrame = new JFrame("Password Manager Client");
    JPanel myPanel = new JPanel();
    PasswordClient client;
    int port = 50501;
    String ip = "127.0.0.1";

    GUI() throws IOException {
        client = new PasswordClient();
        client.startConnection(ip, port);

        myPanel.setLayout(new BoxLayout(myPanel,BoxLayout.Y_AXIS));
        authenticatedPage();

        myJFrame.add(myPanel);

        myJFrame.setSize(500, 500);
        myJFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        myJFrame.setLocationRelativeTo(null);
        myJFrame.setVisible(true);
    }

    public void authenticatedPage(){
        myPanel.removeAll();

        myPanel.add(new JLabel("Domain"));
        myPanel.add(new EntryText());
        myPanel.add(new JLabel("Username"));
        myPanel.add(new EntryText());
        myPanel.add(new JLabel("Password"));
        myPanel.add(new EntryText());

        myPanel.add(new JLabel("Search"));
        myPanel.add(new SearchText());
        myPanel.revalidate();
    }

    class EntryText extends JTextField implements ActionListener{

        EntryText(){
            super();
            this.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            byte[] domain = ((EntryText)myPanel.getComponent(1)).getText().getBytes();
            byte[] username = ((EntryText)myPanel.getComponent(3)).getText().getBytes();
            byte[] password = ((EntryText)myPanel.getComponent(5)).getText().getBytes();

            if (domain.length==0 || username.length==0 || password.length==0) {
                System.out.println("empty fields");
                return;
            }

            try {
                CreateEntryHandler handler = new CreateEntryHandler(client.in,client.out,null);
                byte[][] response = handler.clientWriteRead(new byte[][]{domain,username,password});

            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }


        }
    }

    class SearchText extends JTextField implements ActionListener{

        SearchText(){
            super();
            this.addActionListener(this);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            byte[] domain = this.getText().getBytes();
            try {
                ReadEntryHandler handler = new ReadEntryHandler(client.in,client.out,null);
                byte[][] response = handler.clientWriteRead(new byte[][]{domain});
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
