import java.util.HashSet;
import java.util.concurrent.Future;

public class SendPutChunk implements Runnable {
    private final static int MAX_ATTEMPTS = 5;
    private ChannelBackUp channel_backup;
    private Chunk chunk;
    private byte[] body;
    private PeerInformation peer_information;
    private BackUp backUp;
    private int attempt;
    private int wait_factor;
    private Future next_attempt;
    private boolean running;
    private byte[] message;
    private HashSet<String> replicator_peers = new HashSet<>();

    public SendPutChunk(ChannelBackUp channel_backup, Chunk chunk, byte[] body, PeerInformation peer_information,
            BackUp backUp, boolean is_replicator) {
        this.channel_backup = channel_backup;
        this.chunk = chunk;
        this.body = body;
        this.peer_information = peer_information;
        this.backUp = backUp;
        this.attempt = 0;
        this.wait_factor = 1000;
        this.running = true;
        this.message = MessageFactory.createPutchunkMessage(this.peer_information.getPeerId(), this.chunk.getFileId(), this.chunk.getChunkNr(),
                this.chunk.getReplicationDegree(), this.body);
        if(is_replicator) {
            this.replicator_peers.add(this.peer_information.getPeerId());
        }
    }

    public synchronized void notifyResponse(Message message) {
        if (!message.getAction().equals("STORED")) {
            return;
        } else if (!message.getFileId().equals(this.chunk.getFileId())
                || message.getChunkNo() != this.chunk.getChunkNr()) {
            return;
        }

        this.addReplicator(message.getSenderId());

        if(this.replicator_peers.size() == this.chunk.getReplicationDegree()) {
            this.stop();
            if(this.backUp != null) {
                this.backUp.notifyChunk(true, chunk.getFileId(), chunk.getChunkNr());
            }
        }
    }

    private synchronized void increaseAttempts() {
        this.attempt++;
        this.wait_factor *= 2;
    }

    protected synchronized void addReplicator(String sernder_id) {
        this.replicator_peers.add(sernder_id);
    }

    public synchronized void stop() {
        this.next_attempt.cancel(true);
        this.running = false;
    }

    @Override
    public void run() {
        if(!this.running) {
            return;
        }

        if(this.attempt >= SendPutChunk.MAX_ATTEMPTS) {
            this.stop();
            if(this.backUp != null) {
                this.backUp.notifyChunk(false, chunk.getFileId(), chunk.getChunkNr());
            }
            return;
        }

        this.channel_backup.send_message(this.message);

        this.next_attempt = this.peer_information.getThreadPool().execute(() -> {
            this.increaseAttempts();
            this.run();
        }, this.wait_factor);
    }
}
