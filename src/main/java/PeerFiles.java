import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Collection;


public class PeerFiles implements Serializable {
    /**
     * Contains the files that were backed up
     * Key is file id
     */
    private ConcurrentHashMap<String, FileInfo> backed_up_files;

    /**
     * Contains the files that were backed up
     * Key is file path
     */
    private ConcurrentHashMap<String, String> backed_up_files_by_path;

    public PeerFiles() {
        this.backed_up_files = new ConcurrentHashMap<>();
        this.backed_up_files_by_path = new ConcurrentHashMap<>();
    }

    public void addReplicatorPeer(String sender_id, String file_id, int chunk_no) {
        if(!this.backed_up_files.containsKey(file_id)) {
            return;
        }

        this.backed_up_files.get(file_id).addReplicatorPeer(chunk_no, sender_id);
    }

    public void removeReplicatorPeer(String sender_id, String file_id, int chunk_no) {
        if(!this.backed_up_files.containsKey(file_id)) {
            return;
        }

        this.backed_up_files.get(file_id).removeReplicatorPeer(chunk_no, sender_id);
    }

    public void addFile(FileInfo file_info) {
        this.backed_up_files.put(file_info.getID(), file_info);
        this.backed_up_files_by_path.put(file_info.getPath(), file_info.getID());
    }

    public void removeFileByPath(String path) {
        this.backed_up_files_by_path.remove(path);
    }

    public void removeFileById(String id) {
        this.backed_up_files.remove(id);
    }

	public FileInfo getFile(String id) {
        return this.backed_up_files.get(id);
    }
    
    public FileInfo getFileByPath(String path) {
        System.out.println(path + " " + this.backed_up_files_by_path.size());
        String file_id = this.backed_up_files_by_path.get(path);
        FileInfo file_info = null;
        if(file_id != null) {
            file_info = this.backed_up_files.get(file_id);
        }
        return file_info;
    }

    public Collection<FileInfo> getBackedUpFiles() {
        return this.backed_up_files.values();
    }

	public boolean fileExists(String file_id) {
		return this.backed_up_files.containsKey(file_id);
	}

	public boolean hasFileName(String path) {
		return this.backed_up_files_by_path.containsKey(path);
	}
}