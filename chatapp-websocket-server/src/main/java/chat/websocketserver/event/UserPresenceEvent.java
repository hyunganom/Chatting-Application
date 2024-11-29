package chat.websocketserver.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

/**
 * 사용자 존재 관련 이벤트를 나타내는 클래스.
 * Kafka 메시지로 전송될 때 사용됨.
 */
public class UserPresenceEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String action; // 이벤트 유형 ("JOIN" 또는 "LEAVE")
    private Long userId;        // 사용자 ID
    private Long roomId;        // 채팅방 ID

    /**
     * 기본 생성자. JSON deserialization에 필요함.
     */
    public UserPresenceEvent() {}

    /**
     * 생성자 주입을 통해 필드를 설정함.
     *
     * @param action  이벤트 유형 ("JOIN" 또는 "LEAVE")
     * @param userId  사용자 ID
     * @param roomId  채팅방 ID
     */
    @JsonCreator
    public UserPresenceEvent(
            @JsonProperty("action") String action,
            @JsonProperty("userId") Long userId,
            @JsonProperty("roomId") Long roomId) {
        this.action = action;
        this.userId = userId;
        this.roomId = roomId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserPresenceEvent that = (UserPresenceEvent) o;

        if (!Objects.equals(action, that.action)) return false;
        if (!Objects.equals(userId, that.userId)) return false;
        return Objects.equals(roomId, that.roomId);
    }

    @Override
    public int hashCode() {
        int result = action != null ? action.hashCode() : 0;
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (roomId != null ? roomId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UserPresenceEvent{" +
                "action='" + action + '\'' +
                ", userId=" + userId +
                ", roomId=" + roomId +
                '}';
    }
}
