// ClientObj: class for storing a client and their username
package whiteboard;

public class ClientObj {
    public WBInterface client;
    public String username;

    public ClientObj(WBInterface client, String username) {
        this.client = client;
        this.username = username;
    }
}
