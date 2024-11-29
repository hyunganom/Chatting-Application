package chat.websocketserver.event;

import chat.websocketserver.model.Message;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

/**
 * 메시지 관련 이벤트를 나타내는 클래스.
 * Kafka 메시지로 전송될 때 사용됨.
 */
public class MessageEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String action; // 이벤트 유형 (SEND, DELETE)
    private Message message;    // 관련된 메시지 정보

    /**
     * 기본 생성자. JSON deserialization에 필요함.
     */
    public MessageEvent() {}

    /**
     * 생성자 주입을 통해 필드를 설정함.
     *
     * @param action  이벤트 유형 (SEND, DELETE)
     * @param message 관련된 메시지 정보
     */
    @JsonCreator
    public MessageEvent(
            @JsonProperty("action") String action,
            @JsonProperty("message") Message message) {
        this.action = action;
        this.message = message;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageEvent that = (MessageEvent) o;

        if (!Objects.equals(action, that.action)) return false;
        return Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        int result = action != null ? action.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MessageEvent{" +
                "action='" + action + '\'' +
                ", message=" + message +
                '}';
    }
}
