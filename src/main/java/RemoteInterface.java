import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote {

    int backUp(String filename, int replication_degree) throws RemoteException;

    int restore(String filename) throws RemoteException;

    int restoreEnhaced(String filename) throws RemoteException;

    int delete(String filename) throws RemoteException;

    int reclaim(int space) throws RemoteException;

    String state() throws RemoteException;
}
