package chat.websocketserver.listener;

import chat.websocketserver.event.UserPresenceEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@Slf4j
public class WebSocketEventListener {

    private static final String USER_PRESENCE_TOPIC = "user-presence-events";

    private final KafkaTemplate<String, UserPresenceEvent> kafkaTemplate;

    /**
     * 생성자 주입을 통해 KafkaTemplate을 주입받음.
     *
     * @param kafkaTemplate KafkaTemplate 빈
     */
    public WebSocketEventListener(KafkaTemplate<String, UserPresenceEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * 웹소켓 세션 연결 시 사용자 참여 이벤트를 Kafka에 발행함.
     *
     * @param event 세션 연결 이벤트
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        try {
            StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
            Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
            Long roomId = (Long) headerAccessor.getSessionAttributes().get("roomId");

            if (userId != null && roomId != null) {
                // Kafka 이벤트 발행
                UserPresenceEvent presenceEvent = new UserPresenceEvent("JOIN", userId, roomId);
                kafkaTemplate.send(USER_PRESENCE_TOPIC, presenceEvent);
                log.info("Published JOIN event for User ID: {} in Room ID: {}", userId, roomId);
            } else {
                log.warn("User ID or Room ID is null. Cannot publish JOIN event.");
            }
        } catch (Exception e) {
            log.error("Error processing WebSocketConnectEvent: {}", event, e);
        }
    }

    /**
     * 웹소켓 세션 해제 시 사용자 퇴장 이벤트를 Kafka에 발행함.
     *
     * @param event 세션 해제 이벤트
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        try {
            StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
            Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
            Long roomId = (Long) headerAccessor.getSessionAttributes().get("roomId");

            if (userId != null && roomId != null) {
                // Kafka 이벤트 발행
                UserPresenceEvent presenceEvent = new UserPresenceEvent("LEAVE", userId, roomId);
                kafkaTemplate.send(USER_PRESENCE_TOPIC, presenceEvent);
                log.info("Published LEAVE event for User ID: {} in Room ID: {}", userId, roomId);
            } else {
                log.warn("User ID or Room ID is null. Cannot publish LEAVE event.");
            }
        } catch (Exception e) {
            log.error("Error processing WebSocketDisconnectEvent: {}", event, e);
        }
    }
}
