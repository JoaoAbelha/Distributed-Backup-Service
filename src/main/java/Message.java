import java.net.DatagramPacket;
import java.util.Arrays;

/**
 *
 * By receiving a packet, parses it and runs the corresponding service
 *
 * message format:
 * <Version> <MessageType> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF>
 * */
public class Message {
    private String action;
    private String protocol_version;
    private String sender_id;
    private String file_id;
    private int chunk_no;
    private int replication_degree;
    private byte[] body;
    private int port;
    private int delete_port;

    public Message(DatagramPacket packet) {
        try {
            this.parsePacket(packet);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void parsePacket(DatagramPacket packet) {
        String message = new String(packet.getData());

        message = message.substring(0, Math.min(packet.getLength(), message.length()));
        String[] message_part = message.split("\r\n\r\n", 2);

        message_part[0] = message_part[0].replaceAll("^ +| +$|( )+", "$1").trim();
        String[] message_args = message_part[0].split(" ");
        this.protocol_version = message_args[0];
        this.action = message_args[1];
        this.sender_id = message_args[2];
        boolean has_port_for_delete = this.action.equals("CHECKDELETE") && this.protocol_version.equals("2.0");
        this.file_id = (!has_port_for_delete) ? message_args[3] : null;
        this.delete_port = (has_port_for_delete) ? Integer.parseInt(message_args[3]) : -1;

        this.chunk_no = (message_args.length > 4) ? Integer.parseInt(message_args[4]) : -1;
        boolean has_port_for_restore = this.action.equals("GETCHUNK") && this.protocol_version.equals("2.0");
        this.replication_degree = (message_args.length > 5 && !has_port_for_restore) ? Integer.parseInt(message_args[5]) : -1;
        this.port = (message_args.length > 5 && has_port_for_restore) ? Integer.parseInt(message_args[5]) : -1;
        this.body = (message_part.length == 2) ?  Arrays.copyOfRange(packet.getData(), message_part[0].length() + 5, packet.getLength()) : null;
    }

    public String getAction() {
        return this.action;
    }

	public String getFileId() {
		return this.file_id;
	}

	public int getChunkNo() {
		return this.chunk_no;
	}

	public int getReplicationDegree() {
		return this.replication_degree;
	}

	public byte[] getBody() {
		return this.body;
    }
    
    public String getSenderId() {
        return this.sender_id;
    }

    public String getProtocolVersion() {
        return this.protocol_version;
    }

    public int getPort() {
        return this.port;
    }

    public int getDeletePort() {
        return this.delete_port;
    }
}
