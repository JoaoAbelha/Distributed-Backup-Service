import java.io.IOException;

public class PeerChannels {
    private ChannelBackUp MDB;
    private ChannelRestore MDR;
    private ChannelControl MC;

    public PeerChannels(String[] control_channel, String[] backup_channel, String[] restore_channel, PeerInformation peer_information) throws IOException {
        this.MC = new ChannelControl(control_channel[0], Integer.parseInt(control_channel[1]), peer_information, this);
        this.MDB = new ChannelBackUp(backup_channel[0], Integer.parseInt(backup_channel[1]), peer_information, this);
        this.MDR = new ChannelRestore(restore_channel[0], Integer.parseInt(restore_channel[1]), peer_information, this);

        // so that the peer can listen to different messages arrived at each channel
        new Thread(this.MC).start();
        new Thread(this.MDB).start();
        new Thread(this.MDR).start();
    }

	public ChannelControl getMC() {
		return this.MC;
    }
    
    public ChannelBackUp getMDB() {
		return this.MDB;
    }
    
    public ChannelRestore getMDR() {
		return this.MDR;
    }
}