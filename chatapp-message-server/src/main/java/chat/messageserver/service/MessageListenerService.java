package chat.messageserver.service;

import chat.messageserver.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class MessageListenerService {

    private static final Logger logger = LoggerFactory.getLogger(MessageListenerService.class);

    @Autowired
    private MessageService messageService;

    @KafkaListener(topics = "chat-send", groupId = "message_group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(Message message) {
        if (message != null && message.getRoomId() != null) {
            logger.info("Kafka message received for roomId: {}. Message ID: {}", message.getRoomId(), message.getId());
            try {
                messageService.saveMessage(message.getRoomId(), message);
            } catch (Exception e) {
                logger.error("Error processing message with ID: {} for roomId: {}: {}", message.getId(), message.getRoomId(), e.getMessage());
            }
        } else {
            logger.warn("Invalid or null message received from Kafka: {}", message);
        }
    }
}
