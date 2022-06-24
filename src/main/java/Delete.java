public class Delete {

    private Message message;
    private PeerInformation peer_information;
    private PeerChannels peer_channels;

    public Delete(PeerInformation peer_information, Message message, PeerChannels peer_channels) {
        this.peer_information = peer_information;
        this.message = message;
        this.peer_channels = peer_channels;
        System.out.println("Starting delete protocol");
    }

	public void delteChunks() {
        String file_identifier = message.getFileId();

        this.peer_information.getMemory().removeChunksFromFile(file_identifier);
        this.peer_information.getPeerFiles().removeFileById(file_identifier);
        this.peer_information.addFilesDeleted(file_identifier);

        System.out.println("[Done] Deleted file");
	}

}