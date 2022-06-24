import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentHashMap;

public class SendCheckDelete {
    private final static int TIMEOUT = 15000;
    private PeerInformation peerInformation;
    private PeerChannels peerChannels;
    private int port;
    private ServerSocket server_socket;

    public SendCheckDelete(PeerInformation peerInformation, PeerChannels peerChannels) throws IOException {
        this.peerInformation = peerInformation;
        this.peerChannels = peerChannels;
        this.server_socket = new ServerSocket(0);
        this.port = this.server_socket.getLocalPort();
        this.server_socket.setSoTimeout(SendCheckDelete.TIMEOUT);
    }

    private void getResponse() {
        try {
            Socket socket = this.server_socket.accept();
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            DeletedFiles files_deleted = (DeletedFiles) objectInputStream.readObject();
            this.peerInformation.addFilesToDelete(files_deleted.getFiles());
            objectInputStream.close();
            socket.close();
        } catch (SocketTimeoutException socketTimeoutException) {
            return;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void checkDelete() {
        this.peerInformation.getThreadPool().execute(() -> {
            this.peerChannels.getMC().send_message(MessageFactory.createCHECKDELETE(this.peerInformation.getPeerId(), this.port));
        });

        this.getResponse();
        try {
            this.server_socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}