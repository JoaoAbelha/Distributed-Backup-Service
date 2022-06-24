import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class Chunk implements Serializable {
    private String file_id;
    private int chunk_nr;
    private int size;
    private int replication_degree;
    private ConcurrentHashMap<String, String> replicator_peers;

    public Chunk(String file_id, int number, int size, int replication_degree) {
        this.file_id = file_id;
        this.chunk_nr = number;
        this.size = size;
        this.replication_degree = replication_degree;
        this.replicator_peers = new ConcurrentHashMap<>();
    }

    public String getFileId() {
        return this.file_id;
    }

    public int getChunkNr() {
        return this.chunk_nr;
    }

    public int getReplicationDegree() {
        return this.replication_degree;
    }

    public int getSize() {
        return this.size;
    }

	public void addReplicatorPeer(String sender_id) {
        this.replicator_peers.put(sender_id, "1");
    }
    
    public void removeReplicatorPeer(String sender_id) {
        this.replicator_peers.remove(sender_id);
	}

	public boolean isReplicated() {
		return this.replicator_peers.size() >= this.replication_degree;
	}

	public int getKnownReplicationDegree() {
		return this.replicator_peers.size();
    }
    
    public int getDifferenceRD() {
        return this.replicator_peers.size() - this.replication_degree;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("  id: ").append(this.getFileId() + "_" + this.getChunkNr()).append("\n");
        stringBuilder.append("  size: ").append(this.getSize()/1000).append("\n");
        stringBuilder.append("  replication perceived: ").append(this.getKnownReplicationDegree()).append("\n");

        return stringBuilder.toString();
    }

	public boolean isOverReplicated() {
		return this.replicator_peers.size() > this.replication_degree;
	}
}
