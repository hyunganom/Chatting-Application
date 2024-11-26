package chat.websocketserver.listener;

import chat.websocketserver.event.UserPresenceEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    @Autowired
    private KafkaTemplate<String, UserPresenceEvent> kafkaTemplate;

    private static final String USER_PRESENCE_TOPIC = "user-presence-events";

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        Long roomId = (Long) headerAccessor.getSessionAttributes().get("roomId");

        if (userId != null && roomId != null) {
            // Kafka 이벤트 발행
            UserPresenceEvent presenceEvent = new UserPresenceEvent("JOIN", userId, roomId);
            kafkaTemplate.send(USER_PRESENCE_TOPIC, presenceEvent);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        Long roomId = (Long) headerAccessor.getSessionAttributes().get("roomId");

        if (userId != null && roomId != null) {
            // Kafka 이벤트 발행
            UserPresenceEvent presenceEvent = new UserPresenceEvent("LEAVE", userId, roomId);
            kafkaTemplate.send(USER_PRESENCE_TOPIC, presenceEvent);
        }
    }
}