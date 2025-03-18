// JoinWhiteboard: initialises the GUI and connects to the remote whiteboard (for use by client)
package whiteboard;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import java.awt.EventQueue;


public class JoinWhiteboard {
    public final int SUCCESS = 0;
    public final int USERNAME_TAKEN = 1;
    public final int DENIED = 2;
    public final int ERROR = 3;


    public static String host;
    public static int port;
    public static Gui window;
    public static WBClient client;

    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Usage: java -jar JoinWhiteboard <serverIPAddress> <serverPort>");
            System.exit(1);
        }
        else {
            System.out.println("Server IP Address: " + args[0]);
            System.out.println("Server Port: " + args[1]);
            try {
                host = args[0];
                port = Integer.parseInt(args[1]);
                if (port < 1024 || port > 65535) {
                    throw new Exception();
                }
            } catch (Exception e) {
                // this will occur if the port number can't be converted to integer 
                // or is out of the valid range
                System.out.println("Invalid port number");
                System.exit(1);
            }

        // Set the look and feel to dark mode
        FlatMacDarkLaf.setup();

        // Start the GUI and the personal whiteboard
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                window = new Gui(false);
                try {
                    // get access to the remote whiteboard
                    client = new WBClient(host, port);
                    assert client.remoteWhiteboard != null;
                    window.whiteboard.setRemoteWhiteboard(client.remoteWhiteboard);
                    client.setClientWhiteboard(window.whiteboard);
                    client.setClientGUI(window);
                    int result = client.remoteWhiteboard.registerClient(client, window.whiteboard.username, window.whiteboard.userColor);
                    while (result == WBInterface.USERNAME_TAKEN) {
                        // username already exists
                        window.askAgainForUsername();
                        result = client.remoteWhiteboard.registerClient(client, window.whiteboard.username, window.whiteboard.userColor);
                    }
                    if (result == WBInterface.DENIED) {
                        // manager denied access
                        window.showDeniedMessage();
                        System.exit(1);
                    }
                    else if (result == WBInterface.ERROR) {
                        // error in registering client
                        window.showServerErrorMessage();
                        System.exit(1);
                    }
                } 
                catch (Exception e) {
                    window.showServerErrorMessage();
                    System.exit(1);
                }
            }
        });
        }
    }
}