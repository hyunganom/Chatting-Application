package chat.websocketserver.listener;

import chat.websocketserver.event.ChatRoomEvent;
import chat.websocketserver.event.MessageEvent;
import chat.websocketserver.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WebSocketMessageListenerService {

//    private static final Logger logger = LoggerFactory.getLogger(WebSocketMessageListenerService.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @KafkaListener(
            topics = "message-events",
            groupId = "message_event_group",
            containerFactory = "messageEventKafkaListenerContainerFactory"
    )
    public void consumeMessageEvent(MessageEvent event) {
        if ("SEND".equals(event.getAction())) {
            Message message = event.getMessage();
            Long roomId = message.getRoomId();
            // 브로드캐스트할 메시지 객체 생성 (필요 시 사용자 정보 포함)
            log.info("Broadcasted message to /topic/chatroom-{}", roomId);
            messagingTemplate.convertAndSend("/topic/chatroom-" + roomId, message);
        } else if ("DELETE".equals(event.getAction())) {
            // 메시지 삭제 처리 (필요 시)
            Message message = event.getMessage();
            Long roomId = message.getRoomId();
            messagingTemplate.convertAndSend("/topic/chatroom-" + roomId + "-deletes", message.getId());
            log.info("Broadcasted message deletion to /topic/chatroom-{}-deletes", roomId);
        }
    }

    @KafkaListener(topics = "chatroom-events", groupId = "websocket_group", containerFactory = "kafkaListenerContainerFactory")
    public void consumeChatRoomEvent(ChatRoomEvent event) {
        Long roomId = event.getChatRoom().getId();
        if ("CREATE".equals(event.getAction())) {
            messagingTemplate.convertAndSend("/topic/chatroom-" + roomId, "Chat room created: " + event.getChatRoom().getRoomName());
            log.info("Broadcasted chat room creation to /topic/chatroom-{}", roomId);
        } else if ("UPDATE".equals(event.getAction())) {
            messagingTemplate.convertAndSend("/topic/chatroom-" + roomId, "Chat room updated: " + event.getChatRoom().getRoomName());
            log.info("Broadcasted chat room update to /topic/chatroom-{}", roomId);
        } else if ("DELETE".equals(event.getAction())) {
            messagingTemplate.convertAndSend("/topic/chatroom-" + roomId, "Chat room deleted: " + event.getChatRoom().getRoomName());
            log.info("Broadcasted chat room deletion to /topic/chatroom-{}", roomId);
        }
    }
}
