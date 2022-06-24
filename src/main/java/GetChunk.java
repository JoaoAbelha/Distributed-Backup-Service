import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;

public class GetChunk {
    private PeerInformation peer_information;
    private PeerChannels peer_channels;
    private Message message;

    public GetChunk(Message message, PeerInformation peer_information, PeerChannels peer_channels) {
        this.message = message;
        this.peer_information = peer_information;
        this.peer_channels = peer_channels;
    }

	public void getChunk() {
        String file_id = message.getFileId();
        int chunk_no = message.getChunkNo();
        byte[] chunk_data = this.peer_information.getMemory().getChunkData(file_id, chunk_no);

        if (chunk_data != null) {
            SendChunk sendChunk = new SendChunk(this.peer_channels.getMDR(), file_id, this.peer_information.getPeerId(),
                    chunk_no, chunk_data);

            int sleep_random = ThreadLocalRandom.current().nextInt(0, 400 + 1);
            ScheduledFuture<?> future = this.peer_information.getThreadPool().execute(sendChunk, sleep_random);
            this.peer_channels.getMDR().addChunk(file_id, chunk_no, future);
        }
	}
}
