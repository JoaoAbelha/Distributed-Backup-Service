import java.util.concurrent.ConcurrentHashMap;

public class StoredChunk {
    private Message message;
    private PeerInformation peer_information;
    private ConcurrentHashMap<String, SendPutChunk> requested_put_chunks;

    public StoredChunk(Message message, PeerInformation peer_information, ConcurrentHashMap<String, SendPutChunk> requested_put_chunks) {
        this.message = message;
        this.peer_information = peer_information;
        this.requested_put_chunks = requested_put_chunks;
    }

	public void storedChunk() {
        String sender_id = this.message.getSenderId();
        String file_id = this.message.getFileId();
        int chunk_no = this.message.getChunkNo();
        
        this.peer_information.getMemory().addReplicatorPeer(sender_id, file_id, chunk_no);
        this.peer_information.getPeerFiles().addReplicatorPeer(sender_id, file_id, chunk_no);

        String key = String.format("file%s_chunk%d", message.getFileId(), message.getChunkNo());
        if(this.requested_put_chunks.containsKey(key)) {
            this.requested_put_chunks.get(key).notifyResponse(message);
        }
	}
}