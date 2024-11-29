package chat.messageserver.event;

import chat.messageserver.model.ChatRoom;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * 채팅방 관련 이벤트를 나타내는 클래스.
 * Kafka 메시지로 전송될 때 사용됨.
 */
public class ChatRoomEvent {

    /**
     * 이벤트 유형을 나타내는 열거형.
     */
    public enum ActionType {
        CREATE,
        UPDATE,
        DELETE
    }

    private ActionType action; // 이벤트 유형 (CREATE, UPDATE, DELETE)
    private ChatRoom chatRoom; // 관련된 채팅방 정보

    /**
     * 기본 생성자. JSON deserialization에 필요함.
     */
    public ChatRoomEvent() {}

    /**
     * 생성자 주입을 통해 필드를 설정함.
     *
     * @param action    이벤트 유형 (CREATE, UPDATE, DELETE)
     * @param chatRoom  관련된 채팅방 정보
     */
    @JsonCreator
    public ChatRoomEvent(
            @JsonProperty("action") ActionType action,
            @JsonProperty("chatRoom") ChatRoom chatRoom) {
        if (action == null) {
            throw new IllegalArgumentException("action must not be null");
        }
        if (chatRoom == null) {
            throw new IllegalArgumentException("chatRoom must not be null");
        }
        this.action = action;
        this.chatRoom = chatRoom;
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
        this.action = action;
    }

    /**
     * 관련된 채팅방 정보를 반환함.
     *
     * @return 채팅방 정보
     */
    public ChatRoom getChatRoom() {
        return chatRoom;
    }

    /**
     * 관련된 채팅방 정보를 설정함.
     *
     * @param chatRoom 채팅방 정보
     */
    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChatRoomEvent that = (ChatRoomEvent) o;

        if (action != that.action) return false;
        return Objects.equals(chatRoom, that.chatRoom);
    }

    @Override
    public int hashCode() {
        int result = action != null ? action.hashCode() : 0;
        result = 31 * result + (chatRoom != null ? chatRoom.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ChatRoomEvent{" +
                "action=" + action +
                ", chatRoom=" + chatRoom +
                '}';
    }
}
