
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.rmi.Naming;
import java.rmi.Remote;
import java.util.*;
import java.util.Timer;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * GUI class. Containts everything user can see and click on
 */
public class SciagaczGUI extends JFrame {

    private int filenumber;
    private SciagaczClient client;
    private Sciagacz server;
    java.util.List<String> fileList;
    private JComboBox backupedFiles;

    private Timer timer;
    private TimerTask task;
/**
 * Methos that uses JFileChooser to select file(s) to backup
 */
    private void chooseFile(){
        System.out.println("hfhg");
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        int returnVal = chooser.showOpenDialog(getParent());
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            filenumber = chooser.getSelectedFiles().length;

            String[] filenames = new String[filenumber];

            for(int i =0; i<filenumber; i++){
                System.out.println("You chose to open this file: ");
                filenames[i] = chooser.getCurrentDirectory() + "\\" + chooser.getSelectedFiles()[i].getName();
                System.out.println(filenames[i]);
            }
            uploadFile(filenames);
        }
    }

    /**
     * Method
     * @return
     */
    private Sciagacz loadServer(){
        try {
            String serverName = "rmi://" + client.getIP() + ":" + client.getPort() + "/" + client.getName();
            Remote remote = Naming.lookup(serverName);

            Sciagacz server = null;
            if (remote instanceof Sciagacz)
                server = (Sciagacz) remote;
            else System.out.println("problem z serwerem");

            return server;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    };

    /**
     * Downloads file choosen in combo box. Creates new thread (SwingWorker) that makes it in background.
     * @return True if OK
     */
    private boolean downloadFile(){
        String filename = backupedFiles.getSelectedItem().toString();
        ProgressPanel progressPanel = new ProgressPanel("Pobieranie: " +filename);
        add(progressPanel);
        pack();

        SciagaczGUI tmp = this;

        progressPanel.button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                remove(progressPanel);
                pack();
                tmp.revalidate();
            }
        });

        new SwingWorker<Void,Void>(){
            @Override
            protected Void doInBackground() throws Exception {
                client.receiveFromServer(filename, server, progressPanel.getProgressBar());
                return null;
            }

            @Override
            protected void done() {
                super.done();
                progressPanel.button.setVisible(true);
                pack();
                System.out.println("Done.");

            }
        }.execute();

        return true;
    }

    private boolean uploadFile(String[] filenames){
        for(int i = 0; i<filenames.length; i++ ) {
            //TAK MA BYĆ
            int j = i;

            File file = new File(filenames[i]);
            long date = file.lastModified();

            SciagaczGUI tmp = this;

            if(client.checkDate(filenames[i], date, server))
            {
                JOptionPane.showMessageDialog(tmp, "Plik na serwerze jest młodszy");
            }
            else {
                ProgressPanel progressPanel = new ProgressPanel("Wysyłanie: " + filenames[i]);
                add(progressPanel);
                progressPanel.button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        remove(progressPanel);
                        pack();
                        tmp.revalidate();
                    }
                });
                pack();

                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        client.sendToServer(filenames[j], server, progressPanel.getProgressBar());
                        return null;
                    }

                    @Override
                    protected void done() {
                        super.done();
                        progressPanel.button.setVisible(true);
                        pack();
                        System.out.println("Done.");

                        backupedFiles.removeAllItems();
                        fileList.clear();
                        client.showFilesOnServer(server);
                        fileList = client.getFileList();

                        for (int i = 0; i < fileList.size(); i++)
                            backupedFiles.addItem(fileList.get(i));
                    }
                }.execute();
            }
        }
        return true;
    }

    public Boolean optionBox(){
        JDialog optbox = new JDialog(this, "Opcje");
        optbox.setLocationRelativeTo(null);
        BoxLayout box1 = new BoxLayout(optbox.getContentPane(), BoxLayout.Y_AXIS);
        optbox.setLayout(box1);

        JPanel IPsizeopt = new JPanel();
        IPsizeopt.add(new JLabel("IP serwera:"));
        JTextField iptext = new JTextField();
        iptext.setText(client.getIP());
        IPsizeopt.add(iptext);

        JPanel portopt = new JPanel();
        portopt.add(new JLabel("Port serwera (domyślne 1099):"));
        JTextField porttext = new JTextField();
        porttext.setText(client.getPort());
        portopt.add(porttext);

        JPanel nameopt = new JPanel();
        nameopt.add(new JLabel("Nazwa serwera: "));
        JTextField nametext = new JTextField();
        nametext.setText(client.getName());
        nameopt.add(nametext);

        JPanel packsizeopt = new JPanel();
        packsizeopt.add(new JLabel("rozmiar pakietu (w bajtach): "));
        JTextField packsizetext = new JTextField();
        packsizetext.setText(Integer.toString(client.getPackageSize()));
        packsizeopt.add(packsizetext);

        optbox.add(IPsizeopt);
        optbox.add(portopt);
        optbox.add(nameopt);
        optbox.add(packsizeopt);

        JPanel buttonz = new JPanel();
        JButton okBut =  new JButton("Zapisz");
        JButton nopeBut = new JButton("Anuluj");
        buttonz.add(okBut);
        buttonz.add(nopeBut);
        optbox.add(buttonz);

        optbox.pack();
        optbox.setVisible(true);

        nopeBut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                optbox.dispose();
            }
        });
        okBut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.setIP(iptext.getText());
                client.setPort(porttext.getText());
                client.setName(nametext.getText());
                client.setPackageSize(Integer.parseInt(packsizetext.getText()));
                client.saveSettings(iptext.getText(), porttext.getText(), nametext.getText(), packsizetext.getText());
                optbox.dispose();
            }
        });



        return true;
    }
    /**
     * Constructor. Creates the whole graphic window, buttons and boxes.
     */
    public SciagaczGUI() {
        super();
        client = new SciagaczClient();
        server = loadServer();
        client.showFilesOnServer(server);
        fileList = client.getFileList();

        JButton optionsButton = new JButton("opcje"), backupButton = new JButton("wybierz plik do backupowania"), deleteButton = new JButton("Usuń plik") ,downloadButton = new JButton("pobierz");

        backupedFiles = new JComboBox();

        for (int i = 0; i < fileList.size(); i++)
            backupedFiles.addItem(fileList.get(i));

        BoxLayout boxik = new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS);
        setLayout(boxik);
        setTitle("Java Backuper");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel ins = new JPanel();
        ins.add(optionsButton);
        ins.add(backupButton);
        add(ins);

        add(new Box.Filler(new Dimension(0, 20), new Dimension(0, 20), new Dimension(0, 20)));

        add(backupedFiles);
        add(deleteButton);
        add(downloadButton);
        pack();

        this.setLocationRelativeTo(null);
        this.setVisible(true);


        backupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseFile();
            }
        });

        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                downloadFile();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.deleteFile(backupedFiles.getSelectedItem().toString(),server);

                backupedFiles.removeAllItems();
                fileList.clear();
                client.showFilesOnServer(server);
                fileList = client.getFileList();

                for (int i = 0; i < fileList.size(); i++)
                    backupedFiles.addItem(fileList.get(i));
            }
        });
        optionsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                optionBox();
            }
        });
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                Calendar rightNow = Calendar.getInstance();
                int hour = rightNow.get(Calendar.HOUR_OF_DAY);
                int minute = rightNow.get(Calendar.MINUTE);
                int sec = rightNow.get(Calendar.SECOND);
                System.out.println(hour+":"+minute+":"+sec);

                File folder = new File("cyclic");
                File[] templist = folder.listFiles();
                for (int i = 0; i < templist.length; i++){
                    if(templist[i].isFile())
                        if(!(client.checkDate(templist[i].getName(), templist[i].lastModified(), server)))
                            uploadFile(new String[] {templist[i].getAbsolutePath()});
                }

            }
        };

        timer.scheduleAtFixedRate(task, 0, 10000);
    }

    /**
     * Main function. Starts new GUI.
     * @param args
     */
    public static void main(String[] args){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SciagaczGUI();
            }
        });
    }
}
