import java.io.IOException;
import java.net.DatagramPacket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList; 

public class ChannelControl extends Channel{
    private ConcurrentHashMap<String, SendPutChunk> requested_put_chunks;

    public ChannelControl(String ma, int mp, PeerInformation peer_information, PeerChannels peer_channels) throws IOException {
        super(ma,mp, peer_information, peer_channels);
        this.requested_put_chunks = new ConcurrentHashMap<>();
        System.out.println("Control channel is up");
    }

    public void addPutChunk(String file_id, int chunk_no, SendPutChunk sendPutChunk) {
        String key = String.format("file%s_chunk%d", file_id, chunk_no);
        this.requested_put_chunks.put(key, sendPutChunk);
    }
    
    public void removePutChunk(String file_id, int chunk_no) {
        String key = String.format("file%s_chunk%d", file_id, chunk_no);
        this.requested_put_chunks.remove(key);
	}

    @Override
    protected void handleMessage(DatagramPacket packet) {
        this.handler.execute(() -> {
            Message message = new Message(packet);

            //ignore own messages
            if(message.getSenderId().equals(peer_information.getPeerId())) {
                return;
            }

            System.out.println("[CHANNEL CONTROL]: " + message.getAction() + " " + message.getSenderId());

            if(message.getAction().equals("STORED")) {
                StoredChunk storedChunk = new StoredChunk(message, peer_information, requested_put_chunks);
                storedChunk.storedChunk();
                //this.handler.execute(new StoredChunk(message, peer_information, this.requested_put_chunks));
            } else if(message.getAction().equals("DELETE")) {
                Delete delete = new Delete(peer_information, message, peer_channels);
                delete.delteChunks();
                //this.handler.execute(new Delete(this.peer_information, message, this.peer_channels));
            } else if(message.getAction().equals("REMOVED")) {
                RemovedChunk removedChunk = new RemovedChunk(message, peer_information, peer_channels);
                removedChunk.removedChunk();
                //this.handler.execute(new RemovedChunk(message, this.peer_information, this.peer_channels));
            } else if(message.getAction().equals("GETCHUNK")) {
                if(message.getProtocolVersion().equals("2.0") && message.getProtocolVersion().equals(MessageFactory.protocol_version)) {
                    GetChunkEnhanced getChunkEnhanced = new GetChunkEnhanced(message, peer_information, packet.getAddress());
                    getChunkEnhanced.getChunk();
                    //this.handler.submit(new GetChunkEnhanced(message, peer_information, packet.getAddress()));
                } else {
                    GetChunk getChunk = new GetChunk(message, peer_information, peer_channels);
                    getChunk.getChunk();
                    //this.handler.execute(new GetChunk(message, this.peer_information, this.peer_channels));            
                }
            } else if(message.getAction().equals("CHECKDELETE") && message.getProtocolVersion().equals("2.0") && message.getProtocolVersion().equals(MessageFactory.protocol_version)) { // only takes action if is the correct version
                System.out.println("Received check delete");
                DeleteEnhanced deleteEnhanced = new DeleteEnhanced(message, packet.getAddress(), peer_information);
                deleteEnhanced.sendDeletedFiles();
                //this.handler.execute(new DeleteEnhanced(message, this.peer_information, this.peer_channels));
            }
        });
            
    }
}