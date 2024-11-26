package chat.websocketserver.event;

import chat.websocketserver.model.ChatRoom;

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

    public ChatRoom getChatRoom() {
        return chatRoom;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }
}
