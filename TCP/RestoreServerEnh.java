import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
/**
 * TCP server
 */
public class RestoreServerEnh implements Runnable {

    private ServerSocket socket;
    private boolean running = true;
    // maybe peerinfoimation

    public TCPConnection() {

        try {
            this.socket = new ServerSocket(); // what port ??
        } catch(IOException e) {
            System.out.println("Could not establish a tcp connection");
        }
    }

    @Override
    public void run() {
        while(this.running) {
            Socket client = this.socket.accept();
            new Thread(new handleRestoreEnhanced()).start();
        }
    }
    
}