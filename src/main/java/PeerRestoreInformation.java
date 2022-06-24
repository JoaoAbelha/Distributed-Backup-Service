import java.io.Serializable;

public class PeerRestoreInformation implements Serializable {
    private Memory memory;
    private PeerFiles peer_files;
    private DeletedFiles deletedFiles;

    public PeerRestoreInformation(Memory memory, PeerFiles peerFiles, DeletedFiles deletedFiles) {
        this.memory = memory;
        this.peer_files = peerFiles;
        this.deletedFiles = deletedFiles;
    }

    public Memory getMemory() {
        return this.memory;
    }

    public PeerFiles getPeerFiles() {
        return this.peer_files;
    }

    public DeletedFiles getDeletedFiles() {
        return this.deletedFiles;
    }
}