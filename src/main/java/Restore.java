import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class Restore {
    private final static int MAX_GETCHUNKS = 10;
    private PeerInformation peer_information;
    private PeerChannels peer_channels;
    private String file_name;
    private ConcurrentHashMap<Integer, SendGetChunk> get_chunks_running;
    private ConcurrentHashMap<Integer, byte[]> chunks_body;
    private int number_of_chunks;
    private int received_chunks;
    private int last_chunk_number;
    private ConcurrentHashMap<Integer, Chunk> chunks = new ConcurrentHashMap<>();
    private boolean en;

    public Restore(PeerInformation peer_information, PeerChannels peer_channels, String file_name, boolean en) {
        this.peer_information = peer_information;
        this.peer_channels = peer_channels;
        this.file_name = file_name;
        this.get_chunks_running = new ConcurrentHashMap<>();
        this.chunks_body = new ConcurrentHashMap<>();
        this.received_chunks = 0;
        this.en = en;
    }

    public String getFileName() {
        return this.file_name;
    }

    private synchronized void increment_received_chunks() {
        this.received_chunks++;
    }

    private void stop_get_chunks() {
        for (SendGetChunk sendGetChunk : this.get_chunks_running.values()) {
            sendGetChunk.stop();
        }
    }

    public synchronized void notifyChunk(boolean success, String file_id, int chunk_no, byte[] body) {
        if (!success) {
            System.out.println("Failed to restore file content. SendGetChunk exceeded the number of attempts");
            this.stop_get_chunks();
            return;
        }

        this.increment_received_chunks();
        this.get_chunks_running.remove(chunk_no);
        this.chunks_body.put(chunk_no, body);
        if (!MessageFactory.protocol_version.equals("2.0")) {
            this.peer_channels.getMDR().removeGetChunk(file_id, chunk_no);
        }

        if (this.received_chunks == this.last_chunk_number) {
            System.out.println("Restored file successfully");
            return;
        }

        this.sendGetChunk(this.chunks.get(this.number_of_chunks));
    }

    private void sendGetChunk(Chunk chunk) {
        if (this.get_chunks_running.size() >= MAX_GETCHUNKS || this.getNumberChunks() >= this.last_chunk_number) {
            return;
        }

        SendGetChunk sendGetChunk = null;

        if (en) {
            try {
                sendGetChunk = new SendGetChunkEnhanced(peer_channels, chunk, peer_information, this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            sendGetChunk = new SendGetChunkNormal(peer_channels, chunk, peer_information, this);
            this.peer_channels.getMDR().addGetChunk(chunk.getFileId(), chunk.getChunkNr(), sendGetChunk);
        }

        this.peer_information.getThreadPool().execute(sendGetChunk);
        this.get_chunks_running.put(chunk.getChunkNr(), sendGetChunk);
        this.incrementNumberChunks();
    }

    private synchronized void incrementNumberChunks() {
        this.number_of_chunks++;
    }

    private synchronized int getNumberChunks() {
        return this.number_of_chunks;
    }

    public void sendGetChunks() {
        FileInfo file_info = this.peer_information.getPeerFiles().getFileByPath(this.file_name);

        if (file_info != null) {
            this.last_chunk_number = file_info.getNrChunks();
            for (Chunk chunk : file_info.getChunks()) {
                this.chunks.put(chunk.getChunkNr(), chunk);
            }

            while(this.get_chunks_running.size() < MAX_GETCHUNKS && this.getNumberChunks() < this.last_chunk_number) {
                this.sendGetChunk(this.chunks.get(this.getNumberChunks()));
            }
        } else {
            System.out.println("unknown file");
        }
    }
}