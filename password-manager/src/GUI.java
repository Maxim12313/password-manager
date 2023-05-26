import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.TreeSet;

public class GUI {

    public static void main(String[] args) throws IOException {
        new GUI();
    }

    JFrame myJFrame = new JFrame("Entry");
    JFrame infoFrame = new JFrame("Info");
    JPanel infoPanel = new JPanel();
    JPanel myPanel = new JPanel();
    TreeSet<String> registeredDomains = new TreeSet<>();
    PasswordClient client;
    int port = 50501;
    String ip = "127.0.0.1";

    GUI() throws IOException {
        client = new PasswordClient();
        client.startConnection(ip, port);
        compileDomainSet();
        entryWriterPage();
        infoPage();
    }

    public void compileDomainSet() throws IOException {
        DomainListHandler handler = new DomainListHandler(client.in, client.out, null);
        Response response = handler.clientWriteRead(null);
        if (response.data==null){
            System.out.println(response.error);
            return;
        }

        for (String domain:response.data){
            registeredDomains.add(domain);
        }


    }


    public void infoPage(){
        infoPanel.setLayout(new BoxLayout(infoPanel,BoxLayout.Y_AXIS));
        infoPanel.add(new JLabel("Search"));
        infoPanel.add(new SearchText());


        for (String domain:registeredDomains){

        }



        infoFrame.add(infoPanel);
        infoFrame.revalidate();

        infoFrame.setSize(500, 500);
        infoFrame.setVisible(true);
    }

    public void entryWriterPage(){

        myPanel.setLayout(new BoxLayout(myPanel,BoxLayout.Y_AXIS));
        myJFrame.add(myPanel);

        myPanel.add(new JLabel("Domain"));
        myPanel.add(new EntryText());
        myPanel.add(new JLabel("Username"));
        myPanel.add(new EntryText());
        myPanel.add(new JLabel("Password"));
        myPanel.add(new EntryText());

        myPanel.revalidate();

        myJFrame.setSize(500, 500);
        myJFrame.setLocationRelativeTo(null);
        myJFrame.setVisible(true);
    }


    class DomainTable extends JTable implements ActionListener{

        DomainTable(int rows){
            super(rows,0);
        }

        @Override
        public void actionPerformed(ActionEvent e) {

        }
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
                Response response = handler.clientWriteRead(new byte[][]{domain,username,password});
                if (response.data==null){
                    System.out.println(response.error);
                    return;
                }

                System.out.println(response.data[0]);

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
                Response response = handler.clientWriteRead(new byte[][]{domain});
                if (response.data==null){
                    System.out.println(response.error);
                    return;
                }

                String[] data = response.data;
                System.out.println("domain: "+data[0]+"    username: "+data[1]+"    password: "+data[2]);


            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
