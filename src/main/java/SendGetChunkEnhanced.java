import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class SendGetChunkEnhanced extends SendGetChunk {
    private final static int TIMEOUT = 15000;
    private int port;
    private ServerSocket server_socket;

    public SendGetChunkEnhanced(PeerChannels peer_channels, Chunk chunk, PeerInformation peer_information,
            Restore restore) throws IOException {
        super(peer_channels, chunk, peer_information, restore);
        this.server_socket = new ServerSocket(0);
        this.port = this.server_socket.getLocalPort();
        this.server_socket.setSoTimeout(SendGetChunkEnhanced.TIMEOUT);
        this.start_read_chunk();
        this.create_message();
    }

    private void start_read_chunk() {
        this.peer_information.getThreadPool().execute(() -> {
            this.read_chunk();
            this.stop();
            try {
                this.server_socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void read_chunk() {
        try {
            Socket socket = this.server_socket.accept();
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            byte[] body = (byte[]) objectInputStream.readObject();
            socket.close();
            this.restore.notifyChunk(true, this.chunk.getFileId(), this.chunk.getChunkNr(), body);
        } catch (SocketTimeoutException socketTimeoutException) {
            this.restore.notifyChunk(false, this.chunk.getFileId(), this.chunk.getChunkNr(), null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void create_message() {
        this.message = MessageFactory.createGETCHUNKEnhanced(this.peer_information.getPeerId(), this.chunk.getFileId(), this.chunk.getChunkNr(), this.port);
    }
}