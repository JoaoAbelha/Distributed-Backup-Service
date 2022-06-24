public class SendChunk implements Runnable {
    private ChannelRestore channel_restore;
    private String file_id;
    private String server_id;
    private int chunk_no;
    private byte[] data;

    public SendChunk(ChannelRestore channel_restore, String file_id, String server_id, int chunk_no, byte[] data) {
        this.channel_restore = channel_restore;
        this.file_id = file_id;
        this.server_id = server_id;
        this.chunk_no = chunk_no;
        this.data = data;
    }

    @Override
    public void run() {
        byte[] message = MessageFactory.createCHUNK(server_id, file_id, chunk_no, data);
        this.channel_restore.send_message(message);
    }
}
