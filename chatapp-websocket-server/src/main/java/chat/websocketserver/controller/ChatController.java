package chat.websocketserver.controller;

import chat.websocketserver.event.MessageEvent;
import chat.websocketserver.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.Map;

@Controller
@Slf4j
public class ChatController {

    private static final String MESSAGE_TOPIC = "message-events";

    private final KafkaTemplate<String, MessageEvent> kafkaTemplate;

    /**
     * 생성자 주입을 통해 KafkaTemplate을 주입받음.
     *
     * @param kafkaTemplate KafkaTemplate 빈
     */
    public ChatController(KafkaTemplate<String, MessageEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * 클라이언트로부터 메시지를 수신하고, 이를 Kafka로 발행함.
     *
     * @param message           클라이언트로부터 수신한 메시지 페이로드
     * @param sessionAttributes 웹소켓 세션의 속성 맵
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload Message message, @Header("simpSessionAttributes") Map<String, Object> sessionAttributes) {
        try {
            Long userId = (Long) sessionAttributes.get("userId");
            Long roomId = (Long) sessionAttributes.get("roomId");

            if (userId == null || roomId == null) {
                log.warn("User ID or Room ID is null. Message not processed.");
                return;
            }

            // 메시지 수신 확인 로그
            log.info("Received message from userId: {}, roomId: {}", userId, roomId);
            log.debug("Message content: {}", message.getContent());

            // 메시지 객체 설정
            message.setUserId(userId);
            message.setRoomId(roomId);
            message.setTimestamp(LocalDateTime.now());

            // 메시지 이벤트 생성
            MessageEvent event = new MessageEvent("SEND", message);

            // Kafka로 메시지 이벤트 발행
            kafkaTemplate.send(MESSAGE_TOPIC, event);
            log.info("MessageEvent sent to Kafka: {}", event);

        } catch (Exception e) {
            log.error("Error processing sendMessage: {}", e.getMessage(), e);
        }
    }
}
