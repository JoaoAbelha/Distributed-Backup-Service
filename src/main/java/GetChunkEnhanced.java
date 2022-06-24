import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class GetChunkEnhanced {
    private Message message;
    private PeerInformation peer_information;
    private InetAddress address;

    public GetChunkEnhanced(Message message, PeerInformation peer_information, InetAddress address) {
        this.message = message;
        this.peer_information = peer_information;
        this.address = address;
    }

	public void getChunk() {
        String file_id = message.getFileId();
        int chunk_no = message.getChunkNo();
        int port = message.getPort();
        byte[] chunk_data = this.peer_information.getMemory().getChunkData(file_id, chunk_no);

        if (chunk_data != null) {
            try {
                Socket socket = new Socket(this.address, port);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.writeObject(chunk_data);
                objectOutputStream.close();
                socket.close();
            } catch (IOException e) {
            }
        }
	}
    

}
