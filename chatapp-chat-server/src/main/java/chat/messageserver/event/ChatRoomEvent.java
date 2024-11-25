package chat.messageserver.event;

import chat.messageserver.model.ChatRoom;

public class ChatRoomEvent {
    private String action; // CREATE, UPDATE, DELETE
    private ChatRoom chatRoom;

    public ChatRoomEvent() {}

    public ChatRoomEvent(String action, ChatRoom chatRoom) {
        this.action = action;
        this.chatRoom = chatRoom;
    }

    // Getters and Setters
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public ChatRoom getChatRoom() {
        return chatRoom;
    }

    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }
}
