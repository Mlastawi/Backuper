import java.io.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

/**
 * Class that implements all the server things. Read which file is backuped and handles client requests
 */
public class SciagaczServer extends UnicastRemoteObject implements Sciagacz {

    public int port;
    public String name;

    public String respond(String input) throws RemoteException{
        String client = "Couldn't find client IP address";
        try {
            client = getClientHost();
        }catch (Exception e){};
        System.out.println("Message incomming: " + input +" from client: " + client);
        return "Kappa " + input;
    }

    public String receiveFile(byte[] input) throws RemoteException{
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("./toSend.properties"));
            String filename = properties.getProperty("filename");
            String extension = properties.getProperty("extension");
            FileOutputStream testFile = new FileOutputStream("./output/"+filename+"_out."+extension,true);
            testFile.write(input);
            testFile.close();
            return "Received file successfully";
        }catch (Exception e ){e.printStackTrace(); return "Error";}
    }

    public String receiveFile(Packagee input) throws RemoteException{
        try {
            String tmp[] = input.getFilename().split("\\\\");
            String newName = tmp[tmp.length-1];
            FileOutputStream file;
            if(input.isFirstPackage()) {
                file = new FileOutputStream("./output/" + newName + ".temp", false);
                file.flush();
                System.out.println("Receiving file " + newName + ".temp");
            }
            else
                file = new FileOutputStream("./output/"+newName+".temp",true);
            file.write(input.getFilesByte());
            file.close();

            File file2 = new File("./output/"+newName+".temp");
            if(file2.length() == input.getFileSize()){
                System.out.println("Plik przesłano");
                File file3 = new File("./output/"+newName);
                if (file3.exists())
                    file3.delete();
                file2.renameTo(file3);
            }


            return "Received file successfully";
        }catch (Exception e ){e.printStackTrace(); return "Error";}
    }

    public Packagee sendFile(String name, boolean first, int start) throws RemoteException{
        try{
            int packageSize = 2048;
            FileInputStream fileToRead = new FileInputStream("./output/"+name);
            int size = fileToRead.available();
            if(start + packageSize>size)
                packageSize = size - start;

            byte[] byteArray = new byte[packageSize];

            fileToRead.skip(start);
            fileToRead.read(byteArray);

            Packagee pack = new Packagee(byteArray, name, size, first);
            fileToRead.close();

            if(start == 0) System.out.println("Sending file " + name +" of size " + size);
            else if(start + packageSize==size) System.out.println("File " + name + " sent successfully");
            return pack;
        }catch (Exception e){e.printStackTrace(); return new Packagee();}

    }

    public String createFile(String name) throws RemoteException{
        File file = new File(name);
        try {
            if(!file.exists()) {
                file.createNewFile();
            }
        }catch (Exception e ){return "Couldn't create file";}
        return "File creation success";
    }

    public List<String> getList() throws RemoteException{
        Vector<String> fileNameList = new Vector<String>();
        File folder = new File("output");
        File[] filelist = folder.listFiles();
        for (int i = 0; i < filelist.length; i++){
            if(filelist[i].isFile())
                fileNameList.add(filelist[i].getName());
        }

        return fileNameList;
    }

    public boolean deleteFile(String filename) throws RemoteException {
        System.out.println("Uruchomiono usuwanie pliku " + filename);
        List<String> list = getList();
        for(int i = 0; i<list.size(); i++) {
            String tmp = list.get(i);
            if(tmp.equals(filename)){
                System.out.println("Usuwam plik " + filename);

                (new File("./output/"+list.get(i))).delete();
            }
        }
        return true;
    }

    SciagaczServer() throws RemoteException{
        super();

        //USUWA NIEDOKOŃCZONE PLIKI
        List<String> list = getList();
        for(int i = 0; i<list.size(); i++) {
            String[] tmp = list.get(i).split("\\.");
            if((tmp.length >1) && (tmp[tmp.length-1].equals("temp")))
                (new File("./output/"+list.get(i))).delete();
        }


        System.out.println("Server started...");


        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("./serverConfig.properties"));
            this.port = Integer.parseInt(properties.getProperty("port"));
            this.name = properties.getProperty("name");
        } catch (Exception e1) {
            System.out.println(e1.toString());
        }
    }

    public static void main(String args[]) {
        //System.setSecurityManager(new SecurityManager());

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            SciagaczServer e = new SciagaczServer();
            LocateRegistry.createRegistry(e.port);
            Naming.rebind(e.name, e);
        } catch (Exception x) {
            System.out.println(x.toString());
        }
    }

    //Jeśli prawda to na serwerze młodsza
    public boolean checkDate(String filename, long date) throws RemoteException{
        File file = new File("./output/"+filename);
        if(!file.exists()) return false;
        long serverDate = file.lastModified();

        if(serverDate > date)
            return true;
        else  return false;
    }
}
