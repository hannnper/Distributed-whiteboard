// RegistryChecker: checks the registry for bound names (used for debugging)
package whiteboard;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RegistryChecker {
    public static void main(String[] args) {
        try {
            if (args.length != 2) {
                System.out.println("Usage: java RegistryChecker <host> <port>");
                System.exit(1);
            }
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            Registry registry = LocateRegistry.getRegistry(host, port);
            String[] boundNames = registry.list();
            for (String name : boundNames) {
                System.out.println(name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
