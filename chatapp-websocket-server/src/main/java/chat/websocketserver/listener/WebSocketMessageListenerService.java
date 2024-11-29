package chat.websocketserver.listener;

import chat.websocketserver.event.ChatRoomEvent;
import chat.websocketserver.event.MessageEvent;
import chat.websocketserver.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WebSocketMessageListenerService {

    private static final String MESSAGE_TOPIC = "message-events";
    private static final String CHATROOM_TOPIC_PREFIX = "/topic/chatroom-";

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 생성자 주입을 통해 SimpMessagingTemplate을 주입받음.
     *
     * @param messagingTemplate 메시징 템플릿
     */
    public WebSocketMessageListenerService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Kafka에서 메시지 이벤트를 소비하여 웹소켓을 통해 브로드캐스트함.
     *
     * @param event 메시지 이벤트
     */
    @KafkaListener(
            topics = MESSAGE_TOPIC,
            groupId = "message_event_group",
            containerFactory = "messageEventKafkaListenerContainerFactory"
    )
    public void consumeMessageEvent(MessageEvent event) {
        try {
            String action = event.getAction();
            Message message = event.getMessage();
            Long roomId = message.getRoomId();

            if ("SEND".equalsIgnoreCase(action)) {
                messagingTemplate.convertAndSend(CHATROOM_TOPIC_PREFIX + roomId, message);
                log.info("Broadcasted SEND message to {}", CHATROOM_TOPIC_PREFIX + roomId);
            } else if ("DELETE".equalsIgnoreCase(action)) {
                messagingTemplate.convertAndSend(CHATROOM_TOPIC_PREFIX + roomId + "-deletes", message.getId());
                log.info("Broadcasted DELETE message to {}", CHATROOM_TOPIC_PREFIX + roomId + "-deletes");
            } else {
                log.warn("Received unknown action type: {}", action);
            }
        } catch (Exception e) {
            log.error("Error processing MessageEvent: {}", event, e);
        }
    }

    /**
     * Kafka에서 채팅방 이벤트를 소비하여 웹소켓을 통해 브로드캐스트함.
     *
     * @param event 채팅방 이벤트
     */
    @KafkaListener(
            topics = "chatroom-events",
            groupId = "websocket_group",
            containerFactory = "chatRoomEventKafkaListenerContainerFactory"
    )
    public void consumeChatRoomEvent(ChatRoomEvent event) {
        try {
            String action = event.getAction();
            Long roomId = event.getChatRoom().getId();
            String roomName = event.getChatRoom().getRoomName();

            switch (action.toUpperCase()) {
                case "CREATE":
                    messagingTemplate.convertAndSend(CHATROOM_TOPIC_PREFIX + roomId, "Chat room created: " + roomName);
                    log.info("Broadcasted CREATE chat room to {}", CHATROOM_TOPIC_PREFIX + roomId);
                    break;
                case "UPDATE":
                    messagingTemplate.convertAndSend(CHATROOM_TOPIC_PREFIX + roomId, "Chat room updated: " + roomName);
                    log.info("Broadcasted UPDATE chat room to {}", CHATROOM_TOPIC_PREFIX + roomId);
                    break;
                case "DELETE":
                    messagingTemplate.convertAndSend(CHATROOM_TOPIC_PREFIX + roomId, "Chat room deleted: " + roomName);
                    log.info("Broadcasted DELETE chat room to {}", CHATROOM_TOPIC_PREFIX + roomId);
                    break;
                default:
                    log.warn("Received unknown action type: {}", action);
            }
        } catch (Exception e) {
            log.error("Error processing ChatRoomEvent: {}", event, e);
        }
    }
}
