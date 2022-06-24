import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class PutChunk {
    private Message message;
    private ChannelControl channel_control;
    private PeerInformation peer_information;
    private String peer_id;

    public PutChunk(Message message, PeerInformation peer_information, ChannelControl channel_control, String peer_id) {
        this.message = message;
        this.peer_information = peer_information;
        this.channel_control = channel_control;
        this.peer_id = peer_id;
    }

    public void sendStored() {
        int sleep_random = ThreadLocalRandom.current().nextInt(0, 400 + 1);
        String file_id = this.message.getFileId();
        int chunk_no = this.message.getChunkNo();

        /*simple strategy for enhance :
            if it has more memory available sleeps during less time
        */
        if (message.getProtocolVersion().equals("2.0")) { /*already waited */
            this.channel_control.send_message(MessageFactory.createStoredMessages(this.peer_id, file_id, chunk_no));
            
        } else { 
            this.peer_information.getThreadPool().execute(
                () -> this.channel_control.send_message(MessageFactory.createStoredMessages(this.peer_id, file_id, chunk_no)),
                sleep_random
            );
        }
    }

	public void putChunk() {
        String file_id = this.message.getFileId();
        int chunk_no = this.message.getChunkNo();

        if(this.peer_information.getPeerFiles().fileExists(file_id)) {
            return;
        }
        
        if (message.getProtocolVersion().equals("2.0")) {
            Chunk chunk = this.peer_information.getMemory().getChunk(file_id, chunk_no);
            if(chunk.isOverReplicated()) {
                this.peer_information.getMemory().removeChunk(file_id, chunk_no);
                return;
            }
        }
 
        /* verifica se ja existe ou se tem espaço para dar store*/
        if(this.peer_information.getMemory().storeChumk(message, channel_control, peer_id)) {
            sendStored();
        }
	}
}
