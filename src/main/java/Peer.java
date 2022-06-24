import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Peer implements RemoteInterface {
    private String access_point;
    private PeerInformation peer_information;
    private PeerChannels peer_channels;
    private String version;

    public static void main(String[] args) throws InterruptedException {
        if (args.length != 6) {
            System.out.println("Usage: java <protocol version> <peer id> <service access point> <mc> <mdb> <mdr>");
            System.exit(1);
        }

        String protocol_version = args[0];
        String server_id = args[1];
        String access_point = args[2];

        String[] control_channel = args[3].split(":");
        String[] backup_channel = args[4].split(":");
        String[] restore_channel = args[5].split(":");

        try {
            Peer peer = new Peer(protocol_version, server_id, access_point, control_channel, backup_channel,
                    restore_channel);
            RemoteInterface stub = (RemoteInterface) UnicastRemoteObject.exportObject(peer, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(access_point, stub);
            peer.getPeerInformation().readInformation();
            peer.getPeerInformation().shutdownHook();
            peer.getPeerInformation().periodicSave();
            System.out.println("Peer is active...");
            if (MessageFactory.protocol_version.equals("2.0")) {
                SendCheckDelete sendCheckDelete = new SendCheckDelete(peer.getPeerInformation(),
                        peer.getPeerChannels());
                sendCheckDelete.checkDelete();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public Peer(String protocol_version, String server_id, String access_point, String[] control_channel,
            String[] backup_channel, String[] restore_channel) throws IOException {
        this.peer_information = new PeerInformation(server_id);
        this.peer_channels = new PeerChannels(control_channel, backup_channel, restore_channel, peer_information);
        MessageFactory.protocol_version = protocol_version;
        this.access_point = access_point;
        this.version = protocol_version;
    }

    public PeerInformation getPeerInformation() {
        return this.peer_information;
    }

    public PeerChannels getPeerChannels() {
        return this.peer_channels;
    }

    @Override
    public int backUp(String filename, int replication_degree) {
        FileInfo file_info = new FileInfo(filename, replication_degree);

        if (this.peer_information.getPeerFiles().hasFileName(file_info.getPath())) {
            FileInfo old_file_info = this.peer_information.getPeerFiles().getFileByPath(file_info.getPath());
            DeleteInitiator deleteInitiator = new DeleteInitiator(old_file_info, peer_information, peer_channels);
            deleteInitiator.delete();
        }

        this.peer_information.getPeerFiles().addFile(file_info);
        BackUp backUp = new BackUp(file_info, peer_information, peer_channels);
        backUp.sendChunks();
        return 0;
    }

    @Override
    public int restore(String filename) {
        String name = new File(filename).getName();

        Restore restore = new Restore(this.peer_information, this.peer_channels, name, false);
        restore.sendGetChunks();
        return 0;
    }

    @Override
    public int restoreEnhaced(String filename) throws RemoteException {
        if(!MessageFactory.protocol_version.equals("2.0")) {
            System.out.println("Peer does not support this version");
            return -1;
        }

        String name = new File(filename).getName();

        Restore restore = new Restore(this.peer_information, this.peer_channels, name, true);
        restore.sendGetChunks();
        return 0;
    }

    @Override
    public int delete(String filename) {
        String name = new File(filename).getName();

        FileInfo fileInfo = this.peer_information.getPeerFiles().getFileByPath(name);
        DeleteInitiator deleteInit = new DeleteInitiator(fileInfo, this.peer_information, this.peer_channels);
        deleteInit.delete();
        return 0;
    }

    @Override
    public int reclaim(int space) {
        Reclaim reclaim = new Reclaim(peer_information, peer_channels, space * 1000);
        reclaim.reclaim();
        return 0;
    }

    @Override
    public String state() {
        RetrieveLocalState retrieveLocalState = new RetrieveLocalState(peer_information);
        return retrieveLocalState.state();
    }
}
