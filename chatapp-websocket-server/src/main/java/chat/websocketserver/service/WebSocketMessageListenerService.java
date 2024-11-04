package chat.websocketserver.service;

import chat.websocketserver.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketMessageListenerService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketMessageListenerService.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // "chat-send" 토픽을 구독하여 메시지 브로드캐스트
    @KafkaListener(topics = "chat-send", groupId = "websocket_group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(Message message) {
        if (message != null && message.getRoomId() != null) {
            logger.info("Message received from Kafka: {}", message);

            String broadcastTopic = "/topic/chatroom-" + message.getRoomId();
            messagingTemplate.convertAndSend(broadcastTopic, message);
            logger.info("Message broadcasted to WebSocket topic: {}", broadcastTopic);
        } else {
            logger.warn("Invalid message received from Kafka.");
        }
    }
}
