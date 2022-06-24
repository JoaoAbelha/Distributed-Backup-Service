import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Collection;

/*guardar o estado atual dos objetos em arquivos em formato binário para o  computador*/
public class Memory implements Serializable {
    private static final long serialVersionUID = 1L;
    private final static long MEMORY_AVAILABLE_BYTES = 6400000;
    private final static String BACKUP_DIRNAME = "backup";
    private final static String RESTORE_DIRNAME = "restore";

    private long max_used_bytes = MEMORY_AVAILABLE_BYTES;
    private String backup_dirname;
    private String restore_dirname;
    private long memory_used_bytes;
    private ConcurrentHashMap<String, Chunk> stored_chunks; /* that receive through multicast */

    public Memory(String peer_id) {
        this.backup_dirname = String.format("peer%s/%s", peer_id, BACKUP_DIRNAME);
        this.restore_dirname = String.format("peer%s/%s", peer_id, RESTORE_DIRNAME);
        this.memory_used_bytes = 0;
        this.stored_chunks = new ConcurrentHashMap<>();

        new File(this.backup_dirname).mkdirs();
        new File(this.restore_dirname).mkdirs();
    }

    public double getMemoryUsedRatio() {
        return this.memory_used_bytes / this.max_used_bytes;
    }

    public Collection<Chunk> getChunksStored() {
        return this.stored_chunks.values();
    }

    public synchronized boolean isOverCapacity() {
        return this.memory_used_bytes > this.max_used_bytes;
    }

    public synchronized void setAvailableBytes(int space) {
        this.max_used_bytes = space;
    }

    public synchronized void updateMemoryUsed(long bytes) {
        this.memory_used_bytes += bytes;
    }

    public synchronized boolean canStore(long bytes) {
        return this.memory_used_bytes + bytes <= this.max_used_bytes;
    }

    public long getMemoryUsed() {
        return this.memory_used_bytes;
    }

    /* change when max memory is overriden */
    public long getFreeMemory() {
        return this.max_used_bytes - this.memory_used_bytes;
    }

    public void addChunk(String file_id, int chunk_no, int replication_degree, byte[] body, String peer_id) {
        String key = String.format("file%s_chunk%d", file_id, chunk_no);
        if (!this.stored_chunks.containsKey(key)) {
            this.stored_chunks.put(key, new Chunk(file_id, chunk_no, body.length, replication_degree));
            this.stored_chunks.get(key).addReplicatorPeer(peer_id);
        }
    }

    public void removeChunk(String file_id, int chunk_no) {
        String key = String.format("file%s_chunk%d", file_id, chunk_no);
        this.stored_chunks.remove(key);
    }

    public Chunk getChunk(String file_id, int chunk_no) {
        String key = String.format("file%s_chunk%d", file_id, chunk_no);
        return this.stored_chunks.get(key);
    }

    public byte[] getChunkData(String file_id, int chunk_no) {
        if (chunkExists(file_id, chunk_no)) {
            File file = new File(this.getChunkPathInDisk(file_id, chunk_no));
            byte[] chunk = new byte[(int) file.length()];

            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                fileInputStream.read(chunk);
                fileInputStream.close();
                return chunk;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public void removeReplicatorPeer(String sender_id, String file_id, int chunkNr) {
        String key = String.format("file%s_chunk%d", file_id, chunkNr);

        if (!this.stored_chunks.containsKey(key)) {
            return;
        }

        this.stored_chunks.get(key).removeReplicatorPeer(sender_id);
    }

    public void addReplicatorPeer(String sender_id, String file_id, int chunkNr) {
        String key = String.format("file%s_chunk%d", file_id, chunkNr);

        if (!this.stored_chunks.containsKey(key)) {
            return;
        }

        this.stored_chunks.get(key).addReplicatorPeer(sender_id);
    }

    public boolean isChunkReplicated(String file_id, int chunk_no) {
        Chunk chunk = this.getChunk(file_id, chunk_no);
        if(chunk != null) {
            return chunk.isReplicated();
        }

        return true;
    }

    public boolean storeChumk(Message message, ChannelControl channelControl, String peer_id) {
        String file_id = message.getFileId();
        int chunk_no = message.getChunkNo();
        byte[] body = message.getBody();

        if (this.chunkExists(file_id, chunk_no)) {
            System.out.println("chunk already exists");
            return true;
        }

        while (!this.canStore(body.length)) {
            synchronized (this) {
                boolean removed = false;
                for (Chunk chunk : this.getChunksToRemove()) {
                    if (chunk.getSize() == 0) {
                        continue;
                    }

                    if (chunk.isOverReplicated()) {
                        this.removeChunk(chunk.getFileId(), chunk.getChunkNr());
                        this.deleteChunk(chunk.getFileId(), chunk.getChunkNr());
                        channelControl.send_message(MessageFactory.createRECLAIM(peer_id, file_id, chunk_no));
                        removed = true;
                    }

                    break;
                }

                if (!removed) {
                    this.removeChunk(file_id, chunk_no);
                    return false;
                }

            }
        }

        return this.createChunkDir(file_id, chunk_no, body);
    }

    public boolean restoreChunk(int chunk_no, byte[] body, String file_name) {
        String restore_file = String.format("%s/%s", this.restore_dirname, file_name);

        try {
            RandomAccessFile r = new RandomAccessFile(restore_file, "rw");
            r.seek(chunk_no * 64000);
            r.write(body);
            r.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean createChunkDir(String file_id, int chunk_no, byte[] body) {
        try {
            String file_dir = String.format("%s/%s/", this.backup_dirname, file_id);
            new File(file_dir).mkdirs();

            FileOutputStream out = new FileOutputStream(file_dir + "chunk" + chunk_no);
            out.write(body);
            out.close();

            this.updateMemoryUsed(body.length);
            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return false;
    }

    public boolean chunkExists(String file_id, int chunk_no) {
        String file_dir = String.format("%s/%s/chunk%d", this.backup_dirname, file_id, chunk_no);
        File file = new File(file_dir);
        return file.exists();
    }

    public String getChunkPathInDisk(String file_identifier, int chunk_number) {
        String file_dir = String.format("%s/%s/", this.backup_dirname, file_identifier);
        return file_dir + "chunk" + chunk_number;
    }

    public boolean deleteChunk(String file_identifier, int chunk_number) {
        this.removeChunk(file_identifier, chunk_number);

        String path_str = getChunkPathInDisk(file_identifier, chunk_number);

        File file = new File(path_str);
        long length = file.length();
        boolean deleted = file.delete();

        if (deleted) {
            this.updateMemoryUsed(-1 * length);
        }

        return deleted;
    }

    public Map<String, Chunk> getChunks(String file_identifier) {

        String regex = "file" + file_identifier + "_chunk" + "[0-9]+";

        return this.stored_chunks.entrySet().stream().filter(x -> x.getKey().matches(regex))
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
    }

    public List<Chunk> getChunksToRemove() {
        List<Chunk> chunks_remove = new ArrayList<>(this.stored_chunks.values());
        chunks_remove.sort(Comparator.comparingInt(Chunk::getDifferenceRD).reversed());
        return chunks_remove;
    }

    public String getBackupFolder() {
        return this.backup_dirname;
    }

	public void removeChunksFromFile(String file) {
        String file_backup = String.format("%s/%s/", this.backup_dirname, file);
        File file_backup_dir = new File(file_backup);
        
        if (!file_backup_dir.exists()) {
            return;
        }
        
        for (File chunk : Objects.requireNonNull(file_backup_dir.listFiles())) {
            this.updateMemoryUsed(-1 * (int) chunk.length());
            final int chunk_no = Integer.parseInt(chunk.getName().substring(5));
            this.removeChunk(file, chunk_no);
            chunk.delete();
        }

        file_backup_dir.delete();
	}
}
