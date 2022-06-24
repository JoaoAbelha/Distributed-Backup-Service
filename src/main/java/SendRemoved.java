public class SendRemoved implements Runnable {
	private ChannelControl channel_control;
    private String file_id;
    private int chunk_no;
    private String peer_id;

    public SendRemoved(ChannelControl mc, String fileId, int chunkNr, String peer_id) {
        this.channel_control = mc;
        this.file_id = fileId;
        this.chunk_no = chunkNr;
        this.peer_id = peer_id; 
	}

    @Override
    public void run() {
        byte[] message = MessageFactory.createRECLAIM(peer_id, file_id, chunk_no);
        this.channel_control.send_message(message);
    }
}
