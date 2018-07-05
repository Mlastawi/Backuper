import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Created by Maciek on 02/11/2016.
 */
public interface Sciagacz extends Remote{
        public String respond(String input) throws RemoteException;
        public String receiveFile(byte[] input) throws RemoteException;
        public String receiveFile(Packagee input) throws RemoteException;
        public Packagee sendFile(String input, boolean first, int start) throws RemoteException;
        public List<String> getList() throws RemoteException;
        public String createFile(String name) throws RemoteException;
        public boolean checkDate(String filename, long date) throws RemoteException;
        public boolean deleteFile(String filename) throws RemoteException;
}