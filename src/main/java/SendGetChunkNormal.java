public class SendGetChunkNormal extends SendGetChunk {

    public SendGetChunkNormal(PeerChannels peer_channels, Chunk chunk, PeerInformation peer_information,
            Restore restore) {
        super(peer_channels, chunk, peer_information, restore);
        this.create_message();
    }

    @Override
    protected void create_message() {
        this.message = MessageFactory.createGETCHUNK(this.peer_information.getPeerId(), this.chunk.getFileId(),
                this.chunk.getChunkNr());
    }
}