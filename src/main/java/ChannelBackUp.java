
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;

public class ChannelBackUp extends Channel {
    private ConcurrentHashMap<String, ScheduledFuture<?>> put_chunks;

    public ChannelBackUp(String ma, int mp, PeerInformation peer_information, PeerChannels peer_channels) throws IOException {
        super(ma, mp, peer_information, peer_channels);
        this.put_chunks = new ConcurrentHashMap<>();
        System.out.println("Control Channel is up");
    }


    @Override
    protected void handleMessage(DatagramPacket packet) {
        this.handler.execute(() -> {
            Message message = new Message(packet);
            
            //ignore own messages
            if(message.getSenderId().equals(peer_information.getPeerId())) {
                if(message.getAction().equals("PUTCHUNK")) {
                    String file_id = message.getFileId();
                    int chunk_no = message.getChunkNo();
                    if(peer_information.getMemory().chunkExists(file_id, chunk_no)) {
                        int sleep_random = ThreadLocalRandom.current().nextInt(0, 400 + 1);
                        this.handler.execute(() -> {
                            this.peer_channels.getMC().send_message(MessageFactory.createStoredMessages(this.peer_information.getPeerId(), file_id, chunk_no));
                        }, sleep_random);
                    }
                }
                return;
            }

            System.out.println("[CHANNEL BACKUP]: " + message.getAction() + " file_id " + message.getFileId() + " chunk " + message.getChunkNo());

            if(message.getAction().equals("PUTCHUNK")) {
                this.checkPutChunks(message);
                PutChunk putChunk = new PutChunk(message, this.peer_information, this.peer_channels.getMC(), peer_information.getPeerId());
                this.peer_information.getMemory().addChunk(message.getFileId(), message.getChunkNo(), message.getReplicationDegree(), message.getBody(), this.peer_information.getPeerId());
                if(message.getProtocolVersion().equals("2.0") && message.getProtocolVersion().equals(MessageFactory.protocol_version)) {
                    int upper = 401;
                    int lower = (int) (upper * (long) this.peer_information.getMemory().getMemoryUsedRatio());
                    int sleep_random = ThreadLocalRandom.current().nextInt(lower, upper);
                    this.handler.execute(() -> {
                        putChunk.putChunk();
                    }, sleep_random );
                } else {
                    putChunk.putChunk();
                } 
                //this.handler.execute( new PutChunk(message, this.peer_information, this.peer_channels.getMC(), peer_information.getPeerId()));
            }
        });
    }

	public void addPutChunk(String file_id, int chunk_no, ScheduledFuture<?> future) {
        String key = String.format("file%s_chunk%d", file_id, chunk_no);
        this.put_chunks.put(key, future);
    }
    
    public void checkPutChunks(Message message) {
        String file_id = message.getFileId();
        int chunk_no = message.getChunkNo();
        String key = String.format("file%s_chunk%d", file_id, chunk_no);

        if(this.put_chunks.containsKey(key)) {
            ScheduledFuture<?> put_chunk = this.put_chunks.get(key);
            if(!put_chunk.isDone()) {
                put_chunk.cancel(true);
                System.out.println("Stoped putchunk" + key );
            }
            this.put_chunks.remove(key);
        }
    }
}
