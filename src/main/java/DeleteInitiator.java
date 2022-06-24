import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.*;

public class DeleteInitiator {

    private PeerInformation peer_information;
    private PeerChannels peer_channels;
    private FileInfo fileInfo; 

    public DeleteInitiator(FileInfo fileInfo, PeerInformation peer_information, PeerChannels peer_channels) {
        this.peer_information = peer_information;
        this.peer_channels = peer_channels;
        this.fileInfo = fileInfo;
    }

    /**
     * Delete file and update data in PeerFiles
     * Send message to the MC
     */


	public void delete() {
        if (fileInfo == null) {
            System.out.println("File does not exist");
            return;
        }

        /** keep track of the ID of the deleted files */
        this.peer_information.addFilesDeleted(fileInfo.getID());

        byte [] message =  MessageFactory.createDELETE(this.peer_information.getPeerId(), fileInfo.getID());
        this.peer_channels.getMC().send_message(message);

        this.peer_information.getPeerFiles().removeFileById(fileInfo.getID());
        this.peer_information.getPeerFiles().removeFileByPath(fileInfo.getPath());
        this.peer_information.getMemory().removeChunksFromFile(fileInfo.getID());
	}
  
}