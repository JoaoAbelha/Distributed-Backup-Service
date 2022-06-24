import java.io.File;
import java.io.IOException;

public class ShutdownHook implements Runnable {
    private PeerInformation peer_information;

    public ShutdownHook(PeerInformation peer_information) {
        this.peer_information = peer_information;
    }

    @Override
    public void run() {
        try {
            DeleteEmptyDir.deleteEmptyFolter(this.peer_information.getMemory().getBackupFolder());
            this.peer_information.writeInformation();
            this.peer_information.getThreadPool().shutDown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class DeleteEmptyDir {
        private static boolean isFinished = false;
    
        public static void deleteEmptyFolter(String fileLocation) {
            do {
                isFinished = true;
                replaceText(fileLocation);
            } while (!isFinished);
        }
        
        private static void replaceText(String fileLocation) {
            File folder = new File(fileLocation);
            if (!folder.exists()) {return;}
            File[] listofFiles = folder.listFiles();
            if (listofFiles.length == 0) {
                System.out.println("Folder Name :: " + folder.getAbsolutePath() + " is deleted.");
                folder.delete();
                isFinished = false;
            } else {
                for (int j = 0; j < listofFiles.length; j++) {
                    File file = listofFiles[j];
                    if (file.isDirectory()) {
                        replaceText(file.getAbsolutePath());
                    }
                }
            }
        }
    }
}