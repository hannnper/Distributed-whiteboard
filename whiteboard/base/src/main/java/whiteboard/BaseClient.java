// BaseClient: used for testing the GUI and whiteboard
package whiteboard;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import java.awt.EventQueue;


public class BaseClient {
    public static void main(String[] args) {

        // Set the look and feel to dark mode
        FlatMacDarkLaf.setup();

        // Start the GUI
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Gui window = new Gui(true);
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}