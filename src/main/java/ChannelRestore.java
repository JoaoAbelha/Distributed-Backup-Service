import java.io.IOException;
import java.net.DatagramPacket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

public class ChannelRestore extends Channel {
    private ConcurrentHashMap<String, SendGetChunk> requested_get_chunks;
    private ConcurrentHashMap<String, ScheduledFuture<?>> chunks;

    public ChannelRestore(String ma, int mp, PeerInformation peer_information, PeerChannels peer_channels) throws IOException {
        super(ma, mp, peer_information, peer_channels);
        this.requested_get_chunks = new ConcurrentHashMap<>();
        this.chunks = new ConcurrentHashMap<>();
        System.out.println("Restore channel is up");
    }

    public void addGetChunk(String file_id, int chunk_no, SendGetChunk sendGetChunk) {
        String key = String.format("file%s_chunk%d", file_id, chunk_no);
        this.requested_get_chunks.put(key, sendGetChunk);
    }

    public void removeGetChunk(String file_id, int chunk_no) {
        String key = String.format("file%s_chunk%d", file_id, chunk_no);
        this.requested_get_chunks.remove(key);
    }
    
    public void addChunk(String file_id, int chunk_no, ScheduledFuture<?> future) {
        String key = String.format("file%s_chunk%d", file_id, chunk_no);
        this.chunks.put(key, future);
    }

    @Override
    protected void handleMessage(DatagramPacket packet) {
        this.handler.execute(() -> {
            Message message = new Message(packet);

            //ignore own messages
            if(message.getSenderId().equals(peer_information.getPeerId())) {
                return;
            }

            System.out.println("[CHANNEL RESTORE]: " + message.getAction() + " " + message.getChunkNo() + " " + message.getSenderId());

            if(message.getAction().equals("CHUNK")) {
                this.checkChunks(message);
                String key = String.format("file%s_chunk%d", message.getFileId(), message.getChunkNo());
                if(this.requested_get_chunks.containsKey(key)) {
                    this.requested_get_chunks.get(key).notifyResponse(message);
                }
            }
        });
    }

    private void checkChunks(Message message) {
        String file_id = message.getFileId();
        int chunk_no = message.getChunkNo();
        String key = String.format("file%s_chunk%d", file_id, chunk_no);

        if(this.chunks.containsKey(key)) {
            ScheduledFuture<?> chunk = this.chunks.get(key);
            if(!chunk.isDone()) {
                chunk.cancel(true);
            }
            this.chunks.remove(key);
        }
    }
}
