import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class PeerInformation {
    private final static String STORAGE_NAME = "storage.ser"; // appropriate extension for serializable

    private String storage_path;
    private String peer_id;
    private Memory memory;
    private PeerFiles peer_files;
    private DeletedFiles files_deleted;
    private ThreadPool thread_pool;

    public PeerInformation(String peer_id) {
        this.storage_path = String.format("peer%s/%s", peer_id, STORAGE_NAME);
        this.peer_id = peer_id;
        this.memory = new Memory(peer_id);
        this.peer_files = new PeerFiles();
        this.thread_pool = new ThreadPool();
        this.files_deleted = new DeletedFiles();
    }

    public void readInformation() throws IOException, ClassNotFoundException {
        if (!new File(this.storage_path).exists()) {
            File file = new File(this.storage_path);
            file.getParentFile().mkdirs(); 
            file.createNewFile();
        } else if(new File(this.storage_path).length() == 0) {
            return;
        } else {
            FileInputStream fileInputStream = new FileInputStream(this.storage_path);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            PeerRestoreInformation information = (PeerRestoreInformation) objectInputStream.readObject();
            objectInputStream.close();
            this.memory = information.getMemory();
            this.peer_files = information.getPeerFiles();
            this.files_deleted = information.getDeletedFiles();
        }
    }

    public void writeInformation() throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(this.storage_path);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(new PeerRestoreInformation(this.memory, this.peer_files, this.files_deleted));
        objectOutputStream.close();
    }

    public void shutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHook(this)));
    }


    public void periodicSave() {
        PeerInformation pi = this;
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    pi.writeInformation();
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 5000, 5000);
    }

    public Memory getMemory() {
        return this.memory;
    }

    public PeerFiles getPeerFiles() {
        return this.peer_files;
    }

    public ThreadPool getThreadPool() {
        return this.thread_pool;
    }

    public String getPeerId() {
        return this.peer_id;
    }

    public void addFilesDeleted(String file_id) {
        this.files_deleted.addFile(file_id);
    }

    public DeletedFiles getDeletedFiles() {
        return this.files_deleted;
    }

    public void addFilesToDelete(ConcurrentHashMap<String, String> files_deleted) { 
        this.files_deleted.addFiles(files_deleted);

        for(ConcurrentHashMap.Entry<String, String> entry : this.files_deleted.getFiles().entrySet()) {
            this.memory.removeChunksFromFile(entry.getKey());
        }
	}

}