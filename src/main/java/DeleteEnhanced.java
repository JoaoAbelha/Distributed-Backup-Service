import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class DeleteEnhanced {

    private Message message;
    private InetAddress address;
    private PeerInformation peer_information;

    public DeleteEnhanced(Message message, InetAddress address, PeerInformation peer_information) {
        this.message = message;
        this.address = address;
        this.peer_information = peer_information;
    }

    public void sendDeletedFiles() {
        DeletedFiles deleted_files = this.peer_information.getDeletedFiles();
        if (deleted_files.getFiles().isEmpty()) {
            return;
        }

        try {
            Socket socket = new Socket(this.address, this.message.getDeletePort());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(deleted_files);
            objectOutputStream.close();
            socket.close();
        } catch (IOException e) {
        }
	}
}