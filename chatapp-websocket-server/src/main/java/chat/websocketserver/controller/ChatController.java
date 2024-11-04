package chat.websocketserver.controller;

import chat.websocketserver.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);  // 로그 객체 생성

    @Autowired
    private KafkaTemplate<String, Message> kafkaTemplate;

    private static final String TOPIC_SEND = "chat-send";  // 메시지 전송을 위한 단일 토픽

    @MessageMapping("/chat/sendMessage")
    public void sendMessage(Message message) {
        // 메시지를 Kafka의 "chat-send" 단일 토픽으로 전송 (키 값을 null로 설정하여 라운드 로빈 분배)
        kafkaTemplate.send(TOPIC_SEND, null, message)
                .addCallback(
                        result -> logger.info("Message sent to Kafka: {}", result.getProducerRecord().topic()),
                        ex -> logger.error("Failed to send message to Kafka: {}", ex.getMessage())
                );
    }
}
