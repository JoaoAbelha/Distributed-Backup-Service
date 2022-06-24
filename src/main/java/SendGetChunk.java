import java.util.concurrent.Future;

public abstract class SendGetChunk implements Runnable {
    protected final static int MAX_ATTEMPTS = 5;
    protected PeerChannels peer_channels;
    protected Chunk chunk;
    protected PeerInformation peer_information;
    protected Restore restore;
    protected int attempt;
    protected int wait_factor;
    protected Future next_attempt;
    protected boolean running;
    protected byte[] message;

    public SendGetChunk(PeerChannels peer_channels, Chunk chunk, PeerInformation peer_information, Restore restore) {
        this.peer_channels = peer_channels;
        this.chunk = chunk;
        this.peer_information = peer_information;
        this.restore = restore;
        this.attempt = 0;
        this.wait_factor = 1000;
        this.running = true;
    }

    public synchronized void notifyResponse(Message message) {
        if(!message.getAction().equals("CHUNK")) {
            return;
        } else if(!message.getFileId().equals(this.chunk.getFileId()) || message.getChunkNo() != this.chunk.getChunkNr()) {
            return;
        }

        byte[] body = message.getBody();

        if(body != null) {
            this.stop();
            this.peer_information.getMemory().restoreChunk(this.chunk.getChunkNr(), message.getBody(), this.restore.getFileName());
            this.restore.notifyChunk(true, chunk.getFileId(), chunk.getChunkNr(), body);
        }
    }

    protected synchronized void increaseAttempts() {
        this.attempt++;
        this.wait_factor *= 2;
    }

    public synchronized void stop() {
        this.next_attempt.cancel(true);
        this.running = false;
    }

    protected abstract void create_message();

    @Override
    public synchronized void run() {
        if(!this.running) {
            return;
        }

        if(this.attempt >= SendGetChunk.MAX_ATTEMPTS) {
            this.stop();
            this.restore.notifyChunk(false, chunk.getFileId(), chunk.getChunkNr(), null);
            return;
        }

        this.peer_channels.getMC().send_message(this.message);

        this.next_attempt = this.peer_information.getThreadPool().execute(() -> {
            this.increaseAttempts();
            this.run();
        }, this.wait_factor);
    }
}
