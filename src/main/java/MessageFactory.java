import java.util.Arrays;

public class MessageFactory {
    private final static String CRLF = "\r\n";
    public static String protocol_version;

    public static byte[] createPutchunkMessage(String server_ID, String file_id, int chunk_no, int replication_degree, byte[] body) {
        StringBuffer content = new StringBuffer();
        content.append(protocol_version).append(" ");
        content.append("PUTCHUNK").append(" ");
        content.append(server_ID).append(" ");
        content.append(file_id).append(" ");
        content.append(chunk_no).append(" ");
        content.append(replication_degree).append(" ");
        content.append(CRLF).append(CRLF);


        byte[] header = content.toString().getBytes();

        byte[] output = new byte[header.length + body.length];
        System.arraycopy(header, 0, output, 0, header.length);
        System.arraycopy(body, 0, output, header.length, body.length);
        return output;
    }

    public static byte [] createStoredMessages(String server_ID, String  file_id, int chunk_no) {
        StringBuffer content = new StringBuffer();
        content.append(protocol_version).append(" ");
        content.append("STORED").append(" ");
        content.append(server_ID).append(" ");
        content.append(file_id).append(" ");
        content.append(chunk_no).append(" ");
        content.append(CRLF).append(CRLF);

       return content.toString().getBytes();
    }

    public static byte[] createGETCHUNK(String server_ID, String file_id, int chunk_no) {
        StringBuffer content = new StringBuffer();
        content.append("1.0").append(" ");
        content.append("GETCHUNK").append(" ");
        content.append(server_ID).append(" ");
        content.append(file_id).append(" ");
        content.append(chunk_no).append(" ");
        content.append(CRLF).append(CRLF);

        return content.toString().getBytes();
    }

    public static byte[] createGETCHUNKEnhanced(String server_ID, String file_id, int chunk_no, int port) {
        StringBuffer content = new StringBuffer();
        content.append("2.0").append(" ");
        content.append("GETCHUNK").append(" ");
        content.append(server_ID).append(" ");
        content.append(file_id).append(" ");
        content.append(chunk_no).append(" ");
        content.append(port).append(" ");
        content.append(CRLF).append(CRLF);

        return content.toString().getBytes();
    }

    public static byte[] createCHUNK(String server_ID, String file_id, int chunk_no, byte[] body) {
        StringBuffer content = new StringBuffer();
        content.append(protocol_version).append(" ");
        content.append("CHUNK").append(" ");
        content.append(server_ID).append(" ");
        content.append(file_id).append(" ");
        content.append(chunk_no).append(" ");
        content.append(CRLF).append(CRLF);

        byte[] header = content.toString().getBytes();

        byte[] output = new byte[header.length + body.length];
        System.arraycopy(header, 0, output, 0, header.length);
        System.arraycopy(body, 0, output, header.length, body.length);

        return output;
    }

    public static byte[] createDELETE(String server_ID, String file_id) {
        StringBuffer content = new StringBuffer();
        content.append(protocol_version).append(" ");
        content.append("DELETE").append(" ");
        content.append(server_ID).append(" ");
        content.append(file_id).append(" ");
        content.append(CRLF).append(CRLF);

        return content.toString().getBytes();
    }

    public static byte[] createRECLAIM(String server_ID, String file_id, int chunk_no){
        StringBuffer content = new StringBuffer();
        content.append(protocol_version).append(" ");
        content.append("REMOVED").append(" ");
        content.append(server_ID).append(" ");
        content.append(file_id).append(" ");
        content.append(chunk_no).append(" ");
        content.append(CRLF).append(CRLF);

        return content.toString().getBytes();
    }

    /**
     * To use in the enhancement delete
     * to check if there are files to delete
     */

    public static byte [] createCHECKDELETE(String server_ID, int port) {
        StringBuffer content = new StringBuffer();
        content.append(protocol_version).append(" ");
        content.append("CHECKDELETE").append(" ");
        content.append(server_ID).append(" ");
        content.append(port).append(" ");
        content.append(CRLF).append(CRLF);

        return content.toString().getBytes();
    }
}
