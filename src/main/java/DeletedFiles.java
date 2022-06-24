import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class DeletedFiles implements Serializable {
    private ConcurrentHashMap<String, String> deleted_files;

    DeletedFiles() {
        this.deleted_files = new ConcurrentHashMap<>();
    }

    public void addFile(String file_id) {
        this.deleted_files.put(file_id, "1");
    }

    public void addFiles(ConcurrentHashMap<String, String> files) {
        this.deleted_files.putAll(files);
    }

	public ConcurrentHashMap<String, String> getFiles() {
		return this.deleted_files;
	}
}