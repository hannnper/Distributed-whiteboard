// WBClient: class that implements the WBInterface on the client side
package whiteboard;

import java.awt.Color;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class WBClient extends UnicastRemoteObject implements WBInterface {
    // tools
    public String FREE_DRAW = "Free Draw";
    public String LINE = "Line";
    public String RECTANGLE = "Rectangle";
    public String OVAL = "Oval";
    public String CIRCLE = "Circle";
    public String TEXT = "Text";
    public String ERASE = "Erase";

    public WBInterface remoteWhiteboard;
    public Whiteboard clientWhiteboard;
    public Gui clientGUI;

    public WBClient(String host, int port) throws RemoteException, MalformedURLException, NotBoundException {
        super();
        remoteWhiteboard = (WBInterface) Naming.lookup("rmi://" + host + ":" + port + "/Whiteboard");
        assert remoteWhiteboard != null;
        System.out.println("Connected to Whiteboard server at " + host + ":" + port);
    }

    public void setClientWhiteboard(Whiteboard clientWhiteboard) {
        // must be called before registerClient or other methods
        this.clientWhiteboard = clientWhiteboard;
    }

    public void setClientGUI(Gui clientGUI) {
        this.clientGUI = clientGUI;
    }

    @Override
    public int registerClient(WBInterface wbsi, String username, Color color) throws RemoteException {
        throw new RemoteException("Server should not call registerClient on client object");
    }

    @Override
    public synchronized void drawElement(Element element) throws RemoteException {
        if (element.tool.equals(LINE) || element.tool.equals(ERASE) || element.tool.equals(FREE_DRAW)) {
            clientWhiteboard.wDrawLine(element.x1, element.y1, element.x2, element.y2, element.drawColor, element.size, false);
        } else if (element.tool.equals(RECTANGLE)) {
            clientWhiteboard.wDrawRect(element.x1, element.y1, element.x2 - element.x1, element.y2 - element.y1, element.drawColor, element.fillColor, element.size, false);
        } else if (element.tool.equals(OVAL)) {
            clientWhiteboard.wDrawOval(element.x1, element.y1, element.x2 - element.x1, element.y2 - element.y1, element.drawColor, element.fillColor, element.size, false);
        } else if (element.tool.equals(CIRCLE)) {
            int diameter = Math.max(Math.abs(element.x2 - element.x1), Math.abs(element.y2 - element.y1));
            clientWhiteboard.wDrawCircle(element.x1, element.y1, diameter, element.drawColor, element.fillColor, element.size, false);
        } else if (element.tool.equals(TEXT)) {
            clientWhiteboard.wDrawText(element.inputText, element.x1, element.y1, element.drawColor, element.size, false);
        }
    }


    @Override
    public void replaceCanvas(byte[] canvas) throws RemoteException {
        new Thread(() -> {
            clientWhiteboard.copyOntoCanvas(canvas);
        }).start();
    }

    @Override
    public void broadcastChatMessage(ChatMessage message) throws RemoteException {
        // client can't broadcast chat messages
    }

    @Override
    public void receiveChatMessage(ChatMessage message) throws RemoteException {
        clientGUI.addChatMessage(message);
    }

    @Override
    public void beKicked() throws RemoteException {
        clientGUI.kickWarning();
        System.exit(0);
    }

    @Override
    public void kickUser(String user) throws RemoteException {
        // client can't kick users
    }

    @Override
    public void broadcastUserList() throws RemoteException {
        // client doesn't need to broadcast user list
    }

    @Override
    public void receiveUserList(ArrayList<ChatUser> users) throws RemoteException {
        clientGUI.updateUserList(users);
    }

    @Override
    public void setBackgroundColor(Color color) throws RemoteException {
        clientWhiteboard.setBackgroundColor(color);
    }

    @Override
    public void setUserColor(String username, Color color) throws RemoteException {
        // server does not call this method on clients
    }

    @Override
    public void close() throws RemoteException {
        clientGUI.showCloseMessage();
        System.exit(0);
    }
}
