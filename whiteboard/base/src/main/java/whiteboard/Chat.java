// Chat: class to store chat messages and users
package whiteboard;

import java.awt.Color;
import java.util.ArrayList;

public class Chat {
    private ArrayList<ChatMessage> chatHistory = new ArrayList<ChatMessage>();
    private ArrayList<ChatUser> chatUsers = new ArrayList<ChatUser>();

    public Chat() {
    }

    public void addChatMessage(ChatMessage message) {
        chatHistory.add(message);
    }

    public ArrayList<ChatMessage> getChatHistory() {
        return chatHistory;
    }

    public void addChatUser(String user, Color color) {
        ChatUser chatUser = new ChatUser(user, color);
        chatUsers.add(chatUser);
    }

    public ArrayList<ChatUser> getChatUsers() {
        return chatUsers;
    }

    public void removeChatUser(String user) {
        for (ChatUser chatUser : chatUsers) {
            if (chatUser.getAuthor().equals(user)) {
                chatUsers.remove(chatUser);
                break;
            }
        }
    }

    public Boolean userExists(String user) {
        for (ChatUser chatUser : chatUsers) {
            if (chatUser.getAuthor().equals(user)) {
                return true;
            }
        }
        return false;
    }

    public void setUserColor(String user, Color color) {
        for (ChatUser chatUser : chatUsers) {
            if (chatUser.getAuthor().equals(user)) {
                chatUser.setColor(color);
                break;
            }
        }
    }
}
