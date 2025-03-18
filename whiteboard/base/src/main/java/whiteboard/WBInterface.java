// WBInterface: interface for the whiteboard
package whiteboard;

import java.awt.Color;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;


public interface WBInterface extends Remote {
    public static final int SUCCESS = 0;
    public static final int USERNAME_TAKEN = 1;
    public static final int DENIED = 2;
    public static final int ERROR = 3;
    
    void drawElement(Element element) throws RemoteException;
    void replaceCanvas(byte[] canvas) throws RemoteException;
    int registerClient(WBInterface client, String username, Color userColor) throws RemoteException;
    void broadcastChatMessage(ChatMessage message) throws RemoteException;
    void receiveChatMessage(ChatMessage message) throws RemoteException;
    void beKicked() throws RemoteException;
    void kickUser(String user) throws RemoteException;
    void broadcastUserList() throws RemoteException;
    void receiveUserList(ArrayList<ChatUser> users) throws RemoteException;
    void setBackgroundColor(Color color) throws RemoteException;
    void setUserColor(String username, Color color) throws RemoteException;
    void close() throws RemoteException;
}
