package chat.websocketserver.listener;

import chat.websocketserver.event.ChatRoomEvent;
import chat.websocketserver.event.MessageEvent;
import chat.websocketserver.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * WebSocket 메시지 관련 이벤트를 처리하는 리스너 서비스.
 */
@Service
@Slf4j
public class WebSocketMessageListenerService {

    private final SimpMessagingTemplate messagingTemplate;

    private static final String MESSAGE_TOPIC = "message-events";
    private static final String CHATROOM_TOPIC = "chatroom-events";
    private static final String ACTION_SEND = "SEND";
    private static final String ACTION_DELETE = "DELETE";
    private static final String ACTION_CREATE = "CREATE";
    private static final String ACTION_UPDATE = "UPDATE";

    @Autowired
    public WebSocketMessageListenerService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Kafka에서 메시지 이벤트를 소비함.
     *
     * @param event 소비된 메시지 이벤트
     */
    @KafkaListener(
            topics = MESSAGE_TOPIC,
            groupId = "message_event_group",
            containerFactory = "messageEventKafkaListenerContainerFactory"
    )
    public void consumeMessageEvent(MessageEvent event) {
        String action = event.getAction();
        Message message = event.getMessage();
        Long roomId = message.getRoomId();

        if (ACTION_SEND.equals(action)) {
            // 메시지를 해당 채팅방에 브로드캐스트함.
            messagingTemplate.convertAndSend("/topic/chatroom-" + roomId, message);
            log.info("Broadcasted message to /topic/chatroom-{}", roomId);
        } else if (ACTION_DELETE.equals(action)) {
            // 메시지 삭제를 해당 채팅방에 브로드캐스트함.
            messagingTemplate.convertAndSend("/topic/chatroom-" + roomId + "-deletes", message.getId());
            log.info("Broadcasted message deletion to /topic/chatroom-{}-deletes", roomId);
        }
    }

    /**
     * Kafka에서 채팅방 이벤트를 소비함.
     *
     * @param event 소비된 채팅방 이벤트
     */
    @KafkaListener(
            topics = CHATROOM_TOPIC,
            groupId = "websocket_group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeChatRoomEvent(ChatRoomEvent event) {
        String action = event.getAction();
        Long roomId = event.getChatRoom().getId();
        String roomName = event.getChatRoom().getRoomName();
        String destination = "/topic/chatroom-" + roomId;

        switch (action) {
            case ACTION_CREATE:
                // 채팅방 생성 알림을 브로드캐스트함.
                messagingTemplate.convertAndSend(destination, "Chat room created: " + roomName);
                log.info("Broadcasted chat room creation to {}", destination);
                break;
            case ACTION_UPDATE:
                // 채팅방 업데이트 알림을 브로드캐스트함.
                messagingTemplate.convertAndSend(destination, "Chat room updated: " + roomName);
                log.info("Broadcasted chat room update to {}", destination);
                break;
            case ACTION_DELETE:
                // 채팅방 삭제 알림을 브로드캐스트함.
                messagingTemplate.convertAndSend(destination, "Chat room deleted: " + roomName);
                log.info("Broadcasted chat room deletion to {}", destination);
                break;
            default:
                log.warn("Unknown action '{}' for ChatRoomEvent", action);
        }
    }
}
