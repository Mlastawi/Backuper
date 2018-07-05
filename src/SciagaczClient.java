import javax.swing.*;
import java.io.*;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.util.List;
import java.util.Properties;

/**
 * Class that works as a client. Setups connection and manages files
 */
public class SciagaczClient {
    private String IP;
    private String port;
    private String name;

    private int packageSize;
    List<String> fileList;

    /**
     * The only constructor. Sets values from properties file.
     */

    SciagaczClient(){
        this.IP = "";
        this.name = "";
        this.port = "";
        this.packageSize = 1024;
        try {
            Properties properties = new Properties();
            try {
                properties.load(new FileInputStream("./client_config.properties"));

                this.IP = properties.getProperty("IP");
                this.port = properties.getProperty("port");
                this.name = properties.getProperty("name");
                this.packageSize = Integer.parseInt(properties.getProperty("packageSize"));
            } catch (Exception e1) {
                System.out.println(e1.toString());
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }


    }

    /**
     * Sends specified file to server.
     * @param filename
     *          File name with directory
     * @param server
     *          Name of server, where we want to send a file
     * @param progressBar
     *          Provies data to show progress bar
     * @return
     *          True if everything OK
     */
    public boolean sendToServer(String filename, Sciagacz server, JProgressBar progressBar){
        try {
            String serverName = "rmi://"+this.IP +":"+this.port+"/"+this.name;
            FileInputStream testFile = new FileInputStream(filename);
            int size = testFile.available();
            if(packageSize>size)
                packageSize = size;
            byte[] byteArray = new byte[packageSize];


            int progress = 0;
            boolean first = true;

            progressBar.setMaximum(size);

            do {
                testFile.read(byteArray);
                if(progress != 0)
                    first = false;
                Packagee pack = new Packagee(byteArray, filename, size, first);
                String result2 = server.receiveFile(pack);

                progress += packageSize;
                if(packageSize>testFile.available()){
                    byteArray = new byte[testFile.available()];
                }

                System.out.println("Sending file "+filename+" to server "+serverName);
                System.out.println("File size: "+size+" bytes");
                System.out.println("Actual progress: "+progress+" bytes out of "+size+" bytes");
                System.out.println("========================================================");
                System.out.println();

                progressBar.setValue(progress);
            }while(testFile.available()>0);
            System.out.println("File sent successfully");
            System.out.println();
            testFile.close();
            return true;
        }catch (ConnectException e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "WYSTĄPIŁ BŁĄD PODCZAS POŁĄCZENIA");
            return false;
        }catch (Exception e1){
            e1.printStackTrace();
            JOptionPane.showMessageDialog(null, "NIEZNANY BŁĄD");
            return false;
        }
    }

    /**
     * Analogous to sendtoserver. Client sends request to server, that he wants specific file
     * @param filename
     *          name of file that client wants
     * @param server
     *          Name of server
     * @param progressBar
     *          Provides data to show progress
     * @return
     *          True if OK
     */
    public boolean receiveFromServer(String filename, Sciagacz server, JProgressBar progressBar){
        try{

            String serverName = "rmi://"+this.IP +":"+this.port+"/"+this.name;

            System.out.println("Requesting file..." + " " + filename);
            boolean keepAlive = true;
            int fileSize = 0;
            int progress = 0;

            Packagee answer;
            do {
                if (progress == 0)
                    answer = server.sendFile(filename, true, progress);
                else
                    answer = server.sendFile(filename, false, progress);
                FileOutputStream testFile;
                if(answer.isFirstPackage()) {
                    fileSize = answer.getFileSize();
                    progressBar.setMaximum(fileSize);
                    testFile = new FileOutputStream("./input/" + answer.getFilename(), false);
                    testFile.flush();
                    System.out.println("Downloading file " + answer.getFilename());
                }
                else
                    testFile = new FileOutputStream("./input/"+answer.getFilename(),true);
                testFile.write(answer.getFilesByte());
                testFile.close();

                progress += answer.getFilesByte().length;
                progressBar.setValue(progress);
                System.out.println("Receiving file "+filename+" from server "+serverName);
                System.out.println("File size: "+fileSize+" bytes");
                System.out.println("Actual progress: "+progress+" bytes out of "+fileSize+" bytes");
                System.out.println("========================================================");
                System.out.println();
                if (progress >= fileSize) keepAlive = false;
            }while (keepAlive);
            return true;
        }catch (ConnectException e){
            e.printStackTrace();
            (new File("./input/"+filename)).delete();
            JOptionPane.showMessageDialog(null, "WYSTĄPIŁ BŁĄD PODCZAS POŁĄCZENIA\nNie pobrano pliku: " + filename);
            return false;
        }catch (Exception e1){

            JOptionPane.showMessageDialog(null, "NIEZNANY BŁĄD");
            return false;
        }
    }

    public boolean deleteFile(String filename, Sciagacz server){
        try{
            return server.deleteFile(filename);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * CLient sends request to server, that he wants to know, which files are backuped
     * @param server
     *          name of server
     * @return
     *          True if OK
     */
    public boolean showFilesOnServer(Sciagacz server){
        try{
            fileList = server.getList();
            String serverName = "rmi://"+this.IP +":"+this.port+"/"+this.name;
            System.out.println("Requesting fileList...");
            fileList = server.getList();

            for (int i=fileList.size()-1; i>=0; i--){
                String[] tmp = fileList.get(i).split("\\.");
                if((tmp.length >1) && (tmp[tmp.length-1].equals("temp")))
                    fileList.remove(i);
            }

            for (int i=0; i<fileList.size(); i++){
                System.out.println(fileList.get(i));
            }

            return true;
        }catch (Exception e){e.printStackTrace(); return false;}
    }

    /**
     * Checks if specified file is already on server in newest version
     * @param filename
     *       Name of file
     * @param date
     *      Date on server
     * @param server
     *      Name of server
     * @return
     *      True if the date is newer than the one in file
     */
    public boolean checkDate(String filename, long date, Sciagacz server){
        try {
            return server.checkDate(filename, date);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveSettings(String nip, String nport, String nname, String nps){
        try {
            Properties properties = new Properties();
                properties.load(new FileInputStream("./client_config.properties"));

                properties.setProperty("IP",nip );
                properties.setProperty("port",nport );
                properties.setProperty("name",nname );
                properties.setProperty("packageSize",nps );
                File propsFile = new File("client_config.properties");
                try {
                    OutputStream out = new FileOutputStream(propsFile);
                    properties.store(out, null);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (Exception e1) {
                System.out.println(e1.toString());
            }
        return true;
    }


    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public int getPackageSize() {
        return packageSize;
    }

    public void setPackageSize(int packageSize) {
        this.packageSize = packageSize;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public List<String> getFileList() {return fileList;}
}
