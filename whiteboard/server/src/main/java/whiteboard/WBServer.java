// WBServer: server implementation of the whiteboard service interface
package whiteboard;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.awt.Color;


public class WBServer extends UnicastRemoteObject implements WBInterface {

    // tools
    public String FREE_DRAW = "Free Draw";
    public String LINE = "Line";
    public String RECTANGLE = "Rectangle";
    public String OVAL = "Oval";
    public String CIRCLE = "Circle";
    public String TEXT = "Text";
    public String ERASE = "Erase";

    public static Whiteboard mainWhiteboard;
    public Gui managerGui;
    public String host;
    public int port;
    private ArrayList<ClientObj> clients = new ArrayList<ClientObj>();
    public Chat chat = new Chat();
    public Color serverColor = new Color(252, 144, 3);
    
    public WBServer(String host, int port, Whiteboard whiteboard) throws RemoteException {
        super();

        mainWhiteboard = whiteboard;
        this.host = host;
        this.port = port;

        chat.addChatUser(whiteboard.username, whiteboard.userColor);

    }

    public void setManagerGui(Gui managerGui) {
        this.managerGui = managerGui;
        managerGui.updateUserList(chat.getChatUsers());
    }

    @Override
    public synchronized void drawElement(Element element) throws RemoteException {

        if (element.tool.equals(FREE_DRAW) || element.tool.equals(ERASE) || element.tool.equals(LINE)) {
            mainWhiteboard.wDrawLine(element.x1, element.y1, element.x2, element.y2, element.drawColor, element.size, false);
        }
        else if (element.tool.equals(RECTANGLE)) {
            int width = element.x2 - element.x1;
            int height = element.y2 - element.y1;
            mainWhiteboard.wDrawRect(element.x1, element.y1, width, height, element.drawColor, element.fillColor, element.size, false);
        }
        else if (element.tool.equals(OVAL)) {
            int height = element.y2 - element.y1;
            int width = element.x2 - element.x1;
            mainWhiteboard.wDrawOval(element.x1, element.y1, width, height, element.drawColor, element.fillColor, element.size, false);
        }
        else if (element.tool.equals(CIRCLE)) {
            int diameter = Math.max(Math.abs(element.x2 - element.x1), Math.abs(element.y2 - element.y1));
            mainWhiteboard.wDrawCircle(element.x1, element.y1, diameter, element.drawColor, element.fillColor, element.size, false);
        }
        else if (element.tool.equals(TEXT)) {
            mainWhiteboard.wDrawText(element.inputText, element.x1, element.y1, element.drawColor, element.size, false);
        }

        // send the update to all clients
        for (ClientObj client : clients) {
            new Thread(() -> {
                try {
                    client.client.drawElement(element);
                } 
                catch (RemoteException e) {
                    System.out.println("Error in drawing on " + client.username + "'s whiteboard");
                    removeClient(client);
                }
            }).start();
        }
    }


    @Override
    public void replaceCanvas(byte[] canvas) throws RemoteException {
        //mainWhiteboard.replaceCanvas(canvas);
        // send the update to all clients
        for (ClientObj client : clients) {
            new Thread(() -> {
                try {
                    client.client.replaceCanvas(canvas);
                } 
                catch (RemoteException e) {
                    System.out.println("Error in replacing canvas on " + client.username + "'s whiteboard");
                    removeClient(client);
                }
            }).start();
        }
    }

    public void startRegistry() {
        try {
            LocateRegistry.createRegistry(port);
            Naming.rebind("rmi://" + host + ":" + port + "/Whiteboard", this);
        } catch (Exception e) {
            e.printStackTrace();
            managerGui.showRegistryErrorMessage();
            System.exit(1);
        }
        mainWhiteboard.setRemoteWhiteboard(this);
        System.out.println("Registry started on " + host + ":" + port);
        
    }

    @Override
    public int registerClient(WBInterface client, String username, Color userColor) throws RemoteException {
        try {
            // check username is unique
            if (chat.userExists(username)) {
                return USERNAME_TAKEN;
            }

            // ask manager to accept join request
            if (!managerGui.acceptJoinRequest(username)) {
                return DENIED;
            }

            // add the user to the chat
            chat.addChatUser(username, userColor);
            
            // let other users know that this user has joined
            sendJoinNotification(username);
        
            clients.add(new ClientObj(client, username));
        
            client.replaceCanvas(mainWhiteboard.getCanvasBytes());
            client.setBackgroundColor(mainWhiteboard.getBackgroundColor());
            broadcastUserList();
            sendWelcomeMessage(client, username);

        } 
        catch (RemoteException e) {
            System.out.println("Error in registerClient on server side when sending canvas to client");
            e.printStackTrace();
            return ERROR;
        }
        return SUCCESS;
    }

    @Override
    public void broadcastChatMessage(ChatMessage message) throws RemoteException {
        receiveChatMessage(message);
        for (ClientObj client : clients) {
            new Thread(() -> {
                try {
                    client.client.receiveChatMessage(message);
                } 
                catch (RemoteException e) {
                    System.out.println("Error in sending chat message to " + client.username);
                    removeClient(client);
                }
            }).start();
        }
    }

    @Override
    public void receiveChatMessage(ChatMessage message) throws RemoteException {
        managerGui.addChatMessage(message);
    }

    @Override
    public void beKicked() throws RemoteException {
        // not implemented for manager as they can't be kicked
    }

    @Override
    public void kickUser(String username) {
        // do this in a new thread to avoid waiting for completion of beKicked
        for (ClientObj client : clients) {
            if (client.username.equals(username)) {
                Thread thread = new Thread(() -> {
                    try {
                        client.client.beKicked();
                    } 
                    catch (RemoteException e) {
                        // This will occur every time since the method will cause the application
                        // to exit so there will be no way to send the response
                        ;
                    }
                });
                thread.start();
                removeClient(client);
                break;
            }
        }
    }

    @Override
    public void broadcastUserList() throws RemoteException {
        ArrayList<ChatUser> users = chat.getChatUsers();
        for (ClientObj client : clients) {
            new Thread(() -> {
                try {
                    client.client.receiveUserList(users);
                } 
                catch (Exception e) {
                    // remove client if they can't receive the user list
                    System.out.println("Couldn't send user list to" + client.username + ", removing client");
                    removeClient(client);
                }
            }).start();
        }
        managerGui.updateUserList(users);
    }

    @Override
    public void receiveUserList(ArrayList<ChatUser> users) throws RemoteException {
        // manager already has the user list
    }

    public void sendWelcomeMessage(WBInterface client, String username) {
        new Thread(() -> {
            try {
                ChatMessage welcomeMsg = new ChatMessage("Server", "Hi " + username + "! Welcome to " + mainWhiteboard.username + "'s whiteboard ", serverColor);
                client.receiveChatMessage(welcomeMsg);
            } 
            catch (RemoteException e) {
                System.out.println("Error in sending welcome message");
                e.printStackTrace();
            }
        }).start();
    }

    public void sendJoinNotification(String username) {
        try {
            ChatMessage joinMsg = new ChatMessage("Server", username + " has joined the whiteboard", serverColor);
            broadcastChatMessage(joinMsg);
        } 
        catch (RemoteException e) {
            System.out.println("Error in sending join notification");
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws RemoteException {
        for (ClientObj client : clients) {
            new Thread(() -> {
                try {
                    client.client.close();
                } 
                catch (RemoteException e) {
                    // This will occur every time since the method will cause the application
                    // to exit so there will be no way to send the response
                    ;
                }
            }).start();
        }
        try {
            Naming.unbind("rmi://" + host + ":" + port + "/Whiteboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeClient(ClientObj client) {
        clients.remove(client);
        chat.removeChatUser(client.username);
        try {
            broadcastUserList();
            broadcastChatMessage(new ChatMessage("Server", client.username + " has left the whiteboard", serverColor));
        }
        catch (RemoteException e) {
            System.out.println("Error in broadcasting user list after removing client");
            e.printStackTrace();
        }

    }

    @Override
    public void setBackgroundColor(Color color) throws RemoteException {
        // main whiteboard will already have background color set
        // send the update to all clients
        for (ClientObj client : clients) {
            new Thread(() -> {
                try {
                    client.client.setBackgroundColor(color);
                } 
                catch (RemoteException e) {
                    System.out.println("Error in setting background color on " + client.username + "'s whiteboard");
                    removeClient(client);
                }
            }).start();
        }
    }

    @Override
    public void setUserColor(String username, Color color) throws RemoteException {
        chat.setUserColor(username, color);
        // send the update to all clients
        broadcastUserList();
    }
}
