// ChatMessage: class for a chat message
package whiteboard;

import java.awt.Color;
import java.io.Serializable;

public class ChatMessage implements Serializable {
    private String author;
    private String message;
    private Color color;

    public ChatMessage(String author, String message, Color color) {
        this.author = author;
        this.message = message;
        this.color = color;
    }

    public String getAuthor() {
        return author;
    }

    public String getMessage() {
        return message;
    }

    public Color getColor() {
        return color;
    }

    public String formatChatMessage() {
        // gets the html colour and make username this colour
        String hex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        return "<html><p><span style=\"color:" + hex + "\">" + author + ": </span> " + message + "</p></html>";
    }

    @Override
    public String toString() {
        return formatChatMessage();
    }

}
