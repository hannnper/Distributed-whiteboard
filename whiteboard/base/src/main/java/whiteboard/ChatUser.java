// ChatUser: class for a chat user
package whiteboard;

import java.awt.Color;
import java.io.Serializable;

public class ChatUser implements Serializable {
    private String author;
    private Color color;

    public ChatUser(String author, Color color) {
        this.author = author;
        this.color = color;
    }

    public String getAuthor() {
        return author;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public String formatChatUser() {
        String hex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        return "<html><span style=\"color:" + hex + "\">" + author + "</span></html>";
    }

    @Override
    public String toString() {
        return formatChatUser();
    }

}
