package chat.websocketserver.listener;

import chat.websocketserver.event.ChatRoomEvent;
import chat.websocketserver.event.MessageEvent;
import chat.websocketserver.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
@Slf4j
public class WebSocketMessageListenerService {

    private final SimpMessagingTemplate messagingTemplate;

    private static final String MESSAGE_TOPIC    = "message-events";
    private static final String CHATROOM_TOPIC   = "chatroom-events";
    private static final String ACTION_SEND      = "SEND";
    private static final String ACTION_DELETE    = "DELETE";
    private static final String ACTION_CREATE    = "CREATE";
    private static final String ACTION_UPDATE    = "UPDATE";

    @Autowired
    public WebSocketMessageListenerService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(
            topics          = MESSAGE_TOPIC,
            groupId         = "message_event_group",
            containerFactory = "messageEventKafkaListenerContainerFactory"
    )
    public void consumeMessageEvent(MessageEvent event) {
        String action = event.getAction();
        Message message = event.getMessage();
        Long roomId = message.getRoomId();

        if (ACTION_SEND.equals(action)) {
            messagingTemplate.convertAndSend("/topic/chatroom-" + roomId, message);
            log.info("Broadcasted message to /topic/chatroom-{}", roomId);

        } else if (ACTION_DELETE.equals(action)) {
            messagingTemplate.convertAndSend("/topic/chatroom-" + roomId + "-deletes", message.getId());
            log.info("Broadcasted message deletion to /topic/chatroom-{}-deletes", roomId);
        }
    }

    @KafkaListener(
            topics           = CHATROOM_TOPIC,
            groupId          = "chatroom_event_group",
            containerFactory = "chatRoomEventKafkaListenerContainerFactory"
    )
    public void consumeChatRoomEvent(ChatRoomEvent event) {
        String action   = event.getAction();
        Long roomId     = event.getChatRoom().getId();
        String roomName = event.getChatRoom().getRoomName();
        String dest     = "/topic/chatroom-" + roomId;

        switch (action) {
            case ACTION_CREATE:
                messagingTemplate.convertAndSend(dest, "Chat room created: " + roomName);
                log.info("Broadcasted chat room creation to {}", dest);
                break;
            case ACTION_UPDATE:
                messagingTemplate.convertAndSend(dest, "Chat room updated: " + roomName);
                log.info("Broadcasted chat room update to {}", dest);
                break;
            case ACTION_DELETE:
                messagingTemplate.convertAndSend(dest, "Chat room deleted: " + roomName);
                log.info("Broadcasted chat room deletion to {}", dest);
                break;
            default:
                log.warn("Unknown action '{}' for ChatRoomEvent", action);
        }
    }
}
