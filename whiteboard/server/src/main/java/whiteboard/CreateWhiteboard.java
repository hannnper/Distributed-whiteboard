// CreateWhiteboard: initialises the remote whiteboard server and manager GUI
package whiteboard;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import java.awt.EventQueue;

public class CreateWhiteboard {

    public static WBServer server;

    public static String host;
    public static int port;

    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Usage: java -jar CreateWhiteboard <serverIPAddress> <serverPort>");
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
        }

        // Set the look and feel to dark mode
        FlatMacDarkLaf.setup();

        // Start the GUI and whiteboard and RMI registry
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Gui window = new Gui(true);
                    Thread serverThread = new Thread(
                        new Runnable() {
                            public void run() {
                                try {
                                    server = new WBServer(host, port, window.whiteboard);
                                    server.setManagerGui(window);
                                    server.startRegistry();
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    );
                    serverThread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }
}
