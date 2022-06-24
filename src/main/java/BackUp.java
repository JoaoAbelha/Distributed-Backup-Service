import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class BackUp {
    private final static int MAX_PUTCHUNKS = 10;
    private FileInfo fileInfo;
    private PeerInformation peer_information;
    private PeerChannels peer_channels;
    private ConcurrentHashMap<Integer, SendPutChunk> put_chunks_running;
    private int number_of_chunks;
    private int received_chunks;
    private int last_chunk_number;
    private ConcurrentHashMap<Integer,byte[]> chunks_data = new ConcurrentHashMap<>();

    public BackUp(FileInfo fileInfo, PeerInformation peer_information, PeerChannels peer_channels) {
        this.fileInfo = fileInfo;
        this.peer_information = peer_information;
        this.peer_channels = peer_channels;
        this.put_chunks_running = new ConcurrentHashMap<>();
        this.received_chunks = 0;
        this.number_of_chunks = 0;
        this.last_chunk_number = 0;
        this.splitFile();
    }

    private synchronized void increment_received_chunks() {
        this.received_chunks++;
    }

    private synchronized void increment_number_chunks() {
        this.number_of_chunks++;
    }

    private void stop_put_chunks() {
        for (SendPutChunk sendPutChunk : this.put_chunks_running.values()) {
            sendPutChunk.stop();
        }
    }

    public synchronized void notifyChunk(boolean success, String file_id, int chunk_no) {
        if (!success) {
            this.stop_put_chunks();
            System.out.println("Failed to backup chunks");
            return;
        }

        this.increment_received_chunks();
        this.put_chunks_running.remove(chunk_no);
        this.peer_channels.getMC().removePutChunk(file_id, chunk_no);

        if (this.received_chunks == this.last_chunk_number) {
            System.out.println("Backup file successfully");
            return;
        }

        byte[] body = this.chunks_data.get(this.number_of_chunks);
        this.sendPutChunk(body, this.number_of_chunks);
    }

    public void sendPutChunk(byte[] body, int chunk_number) {
        if (this.put_chunks_running.size() >= MAX_PUTCHUNKS || this.getNumberChunks() >= this.last_chunk_number) {
            return;
        }

        FileInfo file_info = this.peer_information.getPeerFiles().getFile(fileInfo.getID());
        if(file_info != null) {
            file_info.addChunk(chunk_number, body.length);
        }

        Chunk chunk = file_info.getChunk(chunk_number);

        if(chunk != null) {
            SendPutChunk sendPutChunk = new SendPutChunk(peer_channels.getMDB(), chunk, body, peer_information, this, false);
            this.peer_information.getThreadPool().execute(sendPutChunk);
            this.peer_channels.getMC().addPutChunk(chunk.getFileId(), chunk.getChunkNr(), sendPutChunk);
            this.put_chunks_running.put(chunk.getChunkNr(), sendPutChunk);
            this.increment_number_chunks();
        }
    }

    private synchronized int getNumberChunks() {
        return this.number_of_chunks;
    }

    private void splitFile() {
        int chunkNumber = 0;
        byte[] buffer = new byte[FileInfo.CHUNK_SIZE];
        try {
            FileInputStream fileInputStream = new FileInputStream(this.fileInfo.getFile());
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

            int bytes_read;

            while ((bytes_read = bufferedInputStream.read(buffer)) > 0) {
                byte[] body = Arrays.copyOf(buffer, bytes_read);
                this.chunks_data.put(chunkNumber, body);                
                chunkNumber++;
                buffer = new byte[FileInfo.CHUNK_SIZE];
            }

            if (this.fileInfo.getFile().length() % FileInfo.CHUNK_SIZE == 0) {
                this.chunks_data.put(chunkNumber, new byte[0]); 
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.last_chunk_number = chunkNumber;
    }

    public void sendChunks() {
        while(this.put_chunks_running.size() < MAX_PUTCHUNKS && this.getNumberChunks() < this.last_chunk_number) {
            byte[] body = this.chunks_data.get(this.getNumberChunks());
            this.sendPutChunk(body, this.getNumberChunks());
        }
    }
}
