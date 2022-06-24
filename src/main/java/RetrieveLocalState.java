import java.util.Collection;

public class RetrieveLocalState {

    private PeerInformation peer_information;

    public RetrieveLocalState(PeerInformation peer_information) {
        this.peer_information = peer_information;
    }

    public String state() {
        /**
         * For each file whose backup it has initiated: The file pathname The backup
         * service id of the file The desired replication degree For each chunk of the
         * file: Its id Its perceived replication degree
         */
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Peer ").append(this.peer_information.getPeerId()).append("\n");

        stringBuilder.append("BackedUp Files:\n");
        Collection<FileInfo> files_stored = this.peer_information.getPeerFiles().getBackedUpFiles();
        if (files_stored.isEmpty()) {
            stringBuilder.append("No backup files by this peer.\n");
        } else {
            for (FileInfo info : files_stored) {
                stringBuilder.append(info).append("\n");
            }
        }

        stringBuilder.append("Chunks stored:\n");
        Collection<Chunk> chunks_stored = this.peer_information.getMemory().getChunksStored();

        if (chunks_stored.isEmpty()) {
            stringBuilder.append("No chunks are stored.\n");
        } else {
            for (Chunk chunk : chunks_stored) {
                stringBuilder.append(chunk).append("\n");
            }
        }

        /**
         * The peer's storage capacity, i.e. the maximum amount of disk space that can
         * be used to store chunks, and the amount of storage (both in KBytes) used to
         * backup the chunks.
         */
        Memory memory = this.peer_information.getMemory();
        stringBuilder.append("Storage information:\n");
        stringBuilder.append(" Available memory: ").append(memory.getFreeMemory() / 1000).append("KB").append("\n") ;
        stringBuilder.append(" Used memory: ").append(memory.getMemoryUsed() / 1000).append("KB").append("\n");

        return stringBuilder.toString();
    }
}