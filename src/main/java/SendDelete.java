import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

/**
 * Delete initiator protocol
 */
public class SendDelete implements Runnable {
    private Peer initiator;
    private byte[] message;
    private FileInfo file;
    public SendDelete(Peer peer, byte[] message, FileInfo file) {
        this.initiator = peer;
        this.message = message;
        this.file = file;
    }



    @Override
    public void run() {
        /*
        Memory memory = this.initiator.getMemory();
        
        if (file == null) {
            System.out.println("File is not stored in the peer");
            return;
        }

        this.initiator.getMC().send_message(this.message);
        System.out.println("sent delete");
        try {
            Files.delete(Paths.get(this.file.getPath()));
        } catch (IOException e) {
            System.out.println("Could not delete a file!");
        }

        // delete from file system
        memory.removeStoredFile(file.getID());
        System.out.println("[Deleted file]");*/
    }
}