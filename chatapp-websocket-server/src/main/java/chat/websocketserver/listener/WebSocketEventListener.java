package chat.websocketserver.listener;

import chat.websocketserver.event.UserPresenceEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.context.event.EventListener;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * WebSocket 연결 및 해제 이벤트를 처리하는 리스너.
 */
@Component
public class WebSocketEventListener {

    private final KafkaTemplate<String, UserPresenceEvent> kafkaTemplate;

    private static final String USER_PRESENCE_TOPIC = "user-presence-events";
    private static final String ACTION_JOIN = "JOIN";
    private static final String ACTION_LEAVE = "LEAVE";

    @Autowired
    public WebSocketEventListener(KafkaTemplate<String, UserPresenceEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * WebSocket 연결 시 호출됨.
     *
     * @param event 연결 이벤트
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        Long roomId = (Long) headerAccessor.getSessionAttributes().get("roomId");

        if (userId != null && roomId != null) {
            // 사용자 참여 이벤트를 Kafka에 발행함.
            UserPresenceEvent presenceEvent = new UserPresenceEvent(ACTION_JOIN, userId, roomId);
            kafkaTemplate.send(USER_PRESENCE_TOPIC, presenceEvent);
        }
    }

    /**
     * WebSocket 연결 해제 시 호출됨.
     *
     * @param event 연결 해제 이벤트
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        Long roomId = (Long) headerAccessor.getSessionAttributes().get("roomId");

        if (userId != null && roomId != null) {
            // 사용자 퇴장 이벤트를 Kafka에 발행함.
            UserPresenceEvent presenceEvent = new UserPresenceEvent(ACTION_LEAVE, userId, roomId);
            kafkaTemplate.send(USER_PRESENCE_TOPIC, presenceEvent);
        }
    }
}
