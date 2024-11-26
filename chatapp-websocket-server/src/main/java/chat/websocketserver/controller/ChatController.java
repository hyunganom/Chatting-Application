package chat.websocketserver.controller;

import chat.websocketserver.event.MessageEvent;
import chat.websocketserver.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.Map;

@Controller
public class ChatController {

    @Autowired
    private KafkaTemplate<String, MessageEvent> kafkaTemplate;

    private static final String MESSAGE_TOPIC = "message-events";

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload Message message, @Header("simpSessionAttributes") Map<String, Object> sessionAttributes) {
        Long userId = (Long) sessionAttributes.get("userId");
        Long roomId = (Long) sessionAttributes.get("roomId");

        // 메시지 수신 확인 로그 추가
        System.out.println("Received message from userId: " + userId + ", roomId: " + roomId);
        System.out.println("Message content: " + message.getContent());

        if (userId != null && roomId != null) {
            message.setUserId(userId);
            message.setRoomId(roomId);
            message.setTimestamp(LocalDateTime.now());

            // 메시지 이벤트 생성 및 Kafka로 발행
            MessageEvent event = new MessageEvent("SEND", message);
            kafkaTemplate.send(MESSAGE_TOPIC, event);

            // 메시지 발행 확인 로그 추가
            System.out.println("MessageEvent sent to Kafka: " + event);
        }
    }
}
