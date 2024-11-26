package chat.websocketserver.model;

import java.io.Serializable;

public class ChatRoom implements Serializable {
    private Long id;
    private String roomName;
    // 추가 필드...

    public ChatRoom() {}

    public ChatRoom(Long id, String roomName) {
        this.id = id;
        this.roomName = roomName;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
}
