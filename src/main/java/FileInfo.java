import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.math.BigInteger;
import java.nio.file.attribute.BasicFileAttributes;


public class FileInfo implements Serializable {
    private String ID;
    private File file;
    private int desirable_replication_degree;

    private ConcurrentHashMap<Integer,Chunk> chunks;
    private String path;

    static int CHUNK_SIZE = 64000; /*KB*/

    public FileInfo(String path, int replication_degree) {
        this.file= new File(path);
        this.path = this.file.getName();
        this.desirable_replication_degree = replication_degree;
        this.chunks = new ConcurrentHashMap<>();
        this.ID = createID();
        System.out.println("file_id " + this.ID);
    }

    public String getPath() {
        return path;
    }

    public File getFile() {
        return file;
    }

    public int getDesirable_replication_degree() {
        return desirable_replication_degree;
    }

    public int getNrChunks() {
        return this.chunks.size();
    }

    public List<Chunk> getChunks() {
        return this.chunks.values().stream().collect(Collectors.toList());
    }

    /*id will be created based on file name, date modified, owner*/
    private String createID() {
        String hashSource = this.file.getName() + System.currentTimeMillis();
        try {
            BasicFileAttributes metadata = Files.readAttributes(Paths.get(this.file.getName()), BasicFileAttributes.class);
            hashSource += this.file.getName() + metadata.lastModifiedTime() + metadata.size();
        } catch (IOException e) {
            System.out.println("[LocalFile] - Unable to read file's metadata, using filename only for the chunk");
        }

        System.out.println("plain id: " + hashSource);
        byte[] hash = null;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            hash = digest.digest(hashSource.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return String.format("%064x", new BigInteger(1, hash));
    }


    public String getID() {
        return ID;
    }

    public void addChunk(int chunk_no, int size) {
        this.chunks.put(chunk_no, new Chunk(this.ID, chunk_no, size, this.desirable_replication_degree));
    }

	public void addReplicatorPeer(int chunk_no, String sender_id) {
        if(!this.chunks.containsKey(chunk_no)) {
            return;
        }

        this.chunks.get(chunk_no).addReplicatorPeer(sender_id);
    }
    
    public void removeReplicatorPeer(int chunk_no, String sender_id) {
        if(!this.chunks.containsKey(chunk_no)) {
            return;
        }

        this.chunks.get(chunk_no).removeReplicatorPeer(sender_id);
	}

	public Chunk getChunk(int chunk_no) {
		return this.chunks.get(chunk_no);
    }
    
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(" Name:").append(this.getPath()).append("\n");
        stringBuilder.append(" ID:").append(this.getID()).append("\n");
        stringBuilder.append(" Desire replication degree:").append(this.getDesirable_replication_degree()).append("\n");

        Collection<Chunk> chunks = this.chunks.values();
        if(!chunks.isEmpty()) {
            stringBuilder.append("File Chunks:\n");
            for(Chunk chunk : chunks) {
                stringBuilder.append("  chunk_no: ").append(chunk.getChunkNr()).append("\n");
                stringBuilder.append("  replication perceived: ").append(chunk.getKnownReplicationDegree()).append("\n");
            }
        } 

        stringBuilder.append("\n");

        return stringBuilder.toString();
    }
}
