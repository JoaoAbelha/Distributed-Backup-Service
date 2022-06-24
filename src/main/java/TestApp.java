import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TestApp {
    public static void main(String[] args) throws RemoteException, NotBoundException {
        int arg_len = args.length;

        if (arg_len < 2 || arg_len > 5) {
            System.out.println("Wrong number of arguments");
            System.exit(1);
        }

        RemoteInterface peer = null;

        try {
            Registry registry = LocateRegistry.getRegistry();
            peer = (RemoteInterface) registry.lookup(args[0]);

            String sub_protocol = args[1];

            switch (sub_protocol.toLowerCase()) {
                case "backup":
                    if (arg_len != 4) {
                        System.out.println("Usage: <peer access point> BACKUP <file path> <degree>");
                        System.exit(1);
                    }
                    peer.backUp(args[2], Integer.parseInt(args[3]));
                    break;

                case "restore":
                    if (arg_len != 3) {
                        System.out.println("Usage: <peer access point> RESTORE <file_path>");
                        System.exit(1);
                    }
                    peer.restore(args[2]);
                    break;
                case "restoreenh":
                    if (arg_len != 3) {
                        System.out.println("Usage: <peer access point> RESTOREENH <file_path>");
                        System.exit(1);
                    }
                    peer.restoreEnhaced(args[2]);
                    break;
                case "delete":
                    if (arg_len != 3) {
                        System.out.println("Usage: <peer access point> DELETE <file_path>");
                        System.exit(1);
                    }
                    peer.delete(args[2]);
                    break;
                case "reclaim":
                    if (arg_len != 3) {
                        System.out.println("Usage: <peer access point> RECLAIM <disk_space_kbs>");
                        System.exit(1);
                    }
                    peer.reclaim(Integer.parseInt(args[2]));
                    break;
                case "state":
                    if (arg_len != 2) {
                        System.out.println("Usage: <peer access point> STATE");
                        System.exit(1);
                    }
                    System.out.println(peer.state());
                    break;
                default:
                    System.out.println("Write a valid operation");
                    System.exit(1);
                    break;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            System.exit(1);
        }
    }
}
