package chat.websocketserver.event;

import chat.websocketserver.model.ChatRoom;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

/**
 * 채팅방 관련 이벤트를 나타내는 클래스.
 * Kafka 메시지로 전송될 때 사용됨.
 */
public class ChatRoomEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String action; // 이벤트 유형 (CREATE, UPDATE, DELETE)
    private ChatRoom chatRoom;  // 관련된 채팅방 정보

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
            @JsonProperty("action") String action,
            @JsonProperty("chatRoom") ChatRoom chatRoom) {
        this.action = action;
        this.chatRoom = chatRoom;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChatRoomEvent that = (ChatRoomEvent) o;

        if (!Objects.equals(action, that.action)) return false;
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
                "action='" + action + '\'' +
                ", chatRoom=" + chatRoom +
                '}';
    }
}
