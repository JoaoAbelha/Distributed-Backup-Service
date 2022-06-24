import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


/**
 * Channel that uses UDP to send and receive IP packets
 * */
public abstract class Channel implements Runnable{
    private static final int MAX_SIZE_UDP = 65507;

    protected MulticastSocket socket;
    protected InetAddress address;
    protected int port;
    protected PeerInformation peer_information;
    protected PeerChannels peer_channels;
    protected ThreadPool handler; /* to delegate the protocols to the threads */

    public Channel(String ma, int mp, PeerInformation peer_information, PeerChannels peer_channels) throws IOException {
        this.address =  InetAddress.getByName(ma);
        this.port = mp;
        this.socket = new MulticastSocket(port);
        this.socket.joinGroup(address);
        this.peer_information = peer_information;
        this.peer_channels = peer_channels;
        this.handler =  new ThreadPool();
    }


    public void send_message(byte[] message) {
        DatagramPacket packet = new DatagramPacket(message, message.length, this.address, this.port); // create the packet to send through the socket

        try {
            this.socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void run() {
        while(true) {
            try {
                byte [] message = new byte[MAX_SIZE_UDP];
                DatagramPacket packet = new DatagramPacket(message, message.length);
                this.socket.receive(packet);
                this.handleMessage(packet);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    protected abstract void handleMessage(DatagramPacket packet);
}
