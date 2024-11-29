package chat.messageserver.event;

import chat.messageserver.model.Message;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

/**
 * 메시지 관련 이벤트를 나타내는 클래스.
 * Kafka 메시지로 전송될 때 사용됨.
 */
public class MessageEvent implements Serializable {

    private String action; // 이벤트 유형 (SEND, DELETE)
    private Message message;   // 관련된 메시지 정보

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
        if (action == null || action.trim().isEmpty()) {
            throw new IllegalArgumentException("Action must not be null or empty");
        }
        if (message == null) {
            throw new IllegalArgumentException("Message must not be null");
        }
        this.action = action;
        this.message = message;
    }

    /**
     * 이벤트 유형을 반환함.
     *
     * @return 이벤트 유형
     */
    public String getAction() {
        return action;
    }

    /**
     * 이벤트 유형을 설정함.
     *
     * @param action 이벤트 유형
     */
    public void setAction(String action) {
        if (action == null || action.trim().isEmpty()) {
            throw new IllegalArgumentException("Action must not be null or empty");
        }
        this.action = action;
    }

    /**
     * 관련된 메시지 정보를 반환함.
     *
     * @return 메시지 정보
     */
    public Message getMessage() {
        return message;
    }

    /**
     * 관련된 메시지 정보를 설정함.
     *
     * @param message 메시지 정보
     */
    public void setMessage(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("Message must not be null");
        }
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
