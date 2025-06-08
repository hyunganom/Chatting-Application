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

    private static final String USER_PRESENCE_TOPIC = "user-presence-events";
    private static final String ACTION_JOIN  = "JOIN";
    private static final String ACTION_LEAVE = "LEAVE";

    private final KafkaTemplate<String, UserPresenceEvent> kafka;

    @Autowired
    public WebSocketEventListener(KafkaTemplate<String, UserPresenceEvent> kafka) {
        this.kafka = kafka;
    }

    @EventListener
    public void onConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        publishPresence(accessor, ACTION_JOIN);
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        publishPresence(accessor, ACTION_LEAVE);
    }

    private void publishPresence(StompHeaderAccessor accessor, String action) {
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        Long roomId = (Long) accessor.getSessionAttributes().get("roomId");
        if (userId != null && roomId != null) {
            kafka.send(USER_PRESENCE_TOPIC,
                    new UserPresenceEvent(action, userId, roomId));
        }
    }
}
