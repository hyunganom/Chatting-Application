package chat.messageserver.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

/**
 * 사용자 존재 관련 이벤트를 나타내는 클래스.
 * Kafka 메시지로 전송될 때 사용됨.
 */
public class UserPresenceEvent implements Serializable {

    /**
     * 사용자 존재 이벤트 유형을 나타내는 열거형.
     */
    public enum ActionType {
        JOIN,
        LEAVE
    }

    private ActionType action; // 이벤트 유형 (JOIN, LEAVE)
    private Long userId;       // 사용자 ID
    private Long roomId;       // 채팅방 ID

    /**
     * 기본 생성자. JSON deserialization에 필요함.
     */
    public UserPresenceEvent() {}

    /**
     * 생성자 주입을 통해 필드를 설정함.
     *
     * @param action  이벤트 유형 (JOIN, LEAVE)
     * @param userId  사용자 ID
     * @param roomId  채팅방 ID
     */
    @JsonCreator
    public UserPresenceEvent(
            @JsonProperty("action") ActionType action,
            @JsonProperty("userId") Long userId,
            @JsonProperty("roomId") Long roomId) {
        if (action == null) {
            throw new IllegalArgumentException("ActionType must not be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }
        if (roomId == null) {
            throw new IllegalArgumentException("Room ID must not be null");
        }
        this.action = action;
        this.userId = userId;
        this.roomId = roomId;
    }

    /**
     * 이벤트 유형을 반환함.
     *
     * @return 이벤트 유형
     */
    public ActionType getAction() {
        return action;
    }

    /**
     * 이벤트 유형을 설정함.
     *
     * @param action 이벤트 유형
     */
    public void setAction(ActionType action) {
        if (action == null) {
            throw new IllegalArgumentException("ActionType must not be null");
        }
        this.action = action;
    }

    /**
     * 사용자 ID를 반환함.
     *
     * @return 사용자 ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 사용자 ID를 설정함.
     *
     * @param userId 사용자 ID
     */
    public void setUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }
        this.userId = userId;
    }

    /**
     * 채팅방 ID를 반환함.
     *
     * @return 채팅방 ID
     */
    public Long getRoomId() {
        return roomId;
    }

    /**
     * 채팅방 ID를 설정함.
     *
     * @param roomId 채팅방 ID
     */
    public void setRoomId(Long roomId) {
        if (roomId == null) {
            throw new IllegalArgumentException("Room ID must not be null");
        }
        this.roomId = roomId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserPresenceEvent that = (UserPresenceEvent) o;

        if (action != that.action) return false;
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
                "action=" + action +
                ", userId=" + userId +
                ", roomId=" + roomId +
                '}';
    }
}
