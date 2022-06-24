import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;

public class RemovedChunk {
    private Message message;
    private PeerInformation peer_information;
    private PeerChannels peer_channels;

    public RemovedChunk(Message message, PeerInformation peer_information, PeerChannels peer_channels) {
        this.message = message;
        this.peer_information = peer_information;
        this.peer_channels = peer_channels;
    }

	public void removedChunk() {
        int chunk_no = this.message.getChunkNo();
        String file_id = this.message.getFileId();
        String sender_id = this.message.getSenderId();

        this.peer_information.getMemory().removeReplicatorPeer(sender_id, file_id, chunk_no);
        this.peer_information.getPeerFiles().removeReplicatorPeer(sender_id, file_id, chunk_no);

        if(!this.peer_information.getMemory().isChunkReplicated(file_id, chunk_no)) {
            System.out.println("under replicted "+ this.peer_information.getMemory().getChunk(file_id, chunk_no).getKnownReplicationDegree() +"\n\n\n\n");
            
            byte[] chunk = this.peer_information.getMemory().getChunkData(file_id, chunk_no);
            Chunk chunk_information = this.peer_information.getMemory().getChunk(file_id, chunk_no);

            if(chunk != null && chunk_information != null) {
                SendPutChunk sendPutChunk = new SendPutChunk(this.peer_channels.getMDB(), chunk_information, chunk, this.peer_information, null, true);
                this.peer_channels.getMC().addPutChunk(chunk_information.getFileId(), chunk_information.getChunkNr(), sendPutChunk);

                int sleep_random = ThreadLocalRandom.current().nextInt(0, 400 + 1);
                ScheduledFuture<?> future = this.peer_information.getThreadPool().execute(sendPutChunk, sleep_random);
                this.peer_channels.getMDB().addPutChunk(file_id, chunk_no, future);
            }
        }
	}
}
