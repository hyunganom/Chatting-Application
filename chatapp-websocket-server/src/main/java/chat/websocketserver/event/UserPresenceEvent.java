package chat.websocketserver.event;

import java.io.Serializable;

public class UserPresenceEvent implements Serializable {
    private String action; // "JOIN" 또는 "LEAVE"
    private Long userId;
    private Long roomId;

    // 생성자, 게터, 세터
    public UserPresenceEvent() {}

    public UserPresenceEvent(String action, Long userId, Long roomId) {
        this.action = action;
        this.userId = userId;
        this.roomId = roomId;
    }

    public String getAction() {
        return action;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }
}
