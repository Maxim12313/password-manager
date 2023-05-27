import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.TreeSet;

public class GUI {

    public static void main(String[] args) throws IOException {
        new GUI();
    }

    JFrame myJFrame = new JFrame("Entry");
    JFrame infoFrame = new JFrame("Info");
    JPanel infoPanel = new JPanel();
    JPanel domainPanel = new JPanel();
    JPanel detailsPanel = new JPanel();
    JPanel myPanel = new JPanel();
    TreeSet<String> registeredDomains = new TreeSet<>();
    PasswordClient client;
    int port = 50501;
    String ip = "127.0.0.1";

    GUI() throws IOException {
        client = new PasswordClient();
        client.startConnection(ip, port);
        detailsPanel.setLayout(new BorderLayout(10,10));
        detailsPanel.setBackground(Color.WHITE);
        infoPanel.setLayout(new BorderLayout(10,10));

        JPanel header = new JPanel();

        header.add(new JLabel("Search"));
        header.add(new SearchText());

        infoPanel.add(header,BorderLayout.NORTH);


        compileDomainSet();
        updateEntryList();
        updateDetailsPage(null);
        entryWriterPage();

        infoPanel.add(detailsPanel,BorderLayout.CENTER);
        infoFrame.add(infoPanel);
        infoFrame.revalidate();

        infoFrame.setSize(500, 500);
        infoFrame.setVisible(true);
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


    public void updateDetailsPage(String domain){
        if (domain==null){
            return;
        }

        try {
            ReadEntryHandler handler = new ReadEntryHandler(client.in,client.out,null);
            Response response = handler.clientWriteRead(new byte[][]{domain.getBytes()});

            String[] data;
            if (response.data==null){
                System.out.println(response.error);
                data = new String[3];
            }
            else{
                data = response.data;
            }

            JLabel title = new JLabel("Saved Password");
            title.setFont(new Font("Serif", Font.PLAIN, 50));

            JPanel info = new JPanel();
            info.setLayout(new BoxLayout(info,BoxLayout.Y_AXIS));
            JLabel domainLabel = new JLabel("Domain: "+data[0]);
            JLabel usernameLabel = new JLabel("Userame: "+data[1]);
            JLabel passwordLabel = new JLabel("Password: "+data[2]);

            info.add(domainLabel);
            info.add(usernameLabel);
            info.add(passwordLabel);

            detailsPanel.removeAll();
            detailsPanel.add(info,BorderLayout.CENTER);
            detailsPanel.add(title,BorderLayout.NORTH);
            infoFrame.revalidate();


        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }



    public void updateEntryList(){
        domainPanel.removeAll();

        DefaultListModel<String> model = new DefaultListModel<>();
        for (String domain:registeredDomains){
            model.addElement(domain);
        }


        DomainList list = new DomainList(model);

        JScrollPane scrollPane = new JScrollPane(list);
        infoPanel.add(scrollPane,BorderLayout.WEST);
        infoFrame.revalidate();
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


    class DomainList extends JList implements MouseListener {

        DomainList(DefaultListModel model){
            super(model);
            this.setPreferredSize(new Dimension(100,200));
            this.addMouseListener(this);
        }

        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {
            int row = getSelectedIndex();
            String domain = (String) getSelectedValue();
            if (row!=-1){
                System.out.println(row+"    "+domain);
                entryRead(domain.getBytes());
                updateDetailsPage(domain);
            }

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }


    public void entryRead(byte[] domain){
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
                registeredDomains.add(new String(domain));
                updateEntryList();

                System.out.println(response.data[0]);

            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    class SearchText extends JTextField implements ActionListener{

        SearchText(){
            super(30);
            this.addActionListener(this);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            byte[] domain = this.getText().getBytes();
            entryRead(domain);
        }
    }
}
