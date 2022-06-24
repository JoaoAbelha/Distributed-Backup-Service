public class Reclaim {
    PeerInformation peer_information;
    PeerChannels peer_channels;
    int space;

	public Reclaim(PeerInformation peer_information, PeerChannels peer_channels, int space) {
        this.peer_information = peer_information;
        this.peer_channels = peer_channels;
        this.space = space;
    }
    
    public void sendRemoved(Chunk chunk) {
        byte[] message = MessageFactory.createRECLAIM(this.peer_information.getPeerId(), chunk.getFileId(), chunk.getChunkNr());
        this.peer_channels.getMC().send_message(message);
    }

    public void reclaim() {
        this.peer_information.getMemory().setAvailableBytes(space);
        
        if(this.peer_information.getMemory().isOverCapacity()) {
            for(Chunk chunk : this.peer_information.getMemory().getChunksToRemove()) {
                if(this.peer_information.getMemory().deleteChunk(chunk.getFileId(), chunk.getChunkNr())) {
                    System.out.println("removed chunk " + chunk.getChunkNr());
                    sendRemoved(chunk);
                }

                if(!this.peer_information.getMemory().isOverCapacity()) {
                    break;
                }
            }
        }
    }
}