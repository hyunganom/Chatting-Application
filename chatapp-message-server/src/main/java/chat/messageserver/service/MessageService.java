package chat.messageserver.service;

import chat.messageserver.event.MessageEvent;
import chat.messageserver.model.Message;
import chat.messageserver.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);
    private static final String MESSAGE_TOPIC = "message-events";

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private KafkaTemplate<String, MessageEvent> kafkaTemplate;

    /**
     * 메시지 저장
     */
    public void saveMessage(Message message) {
        // 필수 필드 설정 및 검증
        if (message.getId() == null) {
            // message.setId(UUID.randomUUID().toString()); // ID가 Long 타입이라면 수정 필요
        }
        if (message.getTimestamp() == null) {
            message.setTimestamp(LocalDateTime.now());
        }

        try {
            messageRepository.save(message);
            logger.info("Message with ID: {} saved successfully to the database.", message.getId());
        } catch (Exception e) {
            logger.error("Error saving message with ID: {} to the database: {}", message.getId(), e.getMessage());
        }
    }

    /**
     * 메시지 전송
     */
    public void sendMessage(Message message) {
        // 필수 필드 설정 및 검증
        if (message.getId() == null) {
            // message.setId(UUID.randomUUID().toString()); // ID가 Long 타입이라면 수정 필요
        }
        if (message.getTimestamp() == null) {
            message.setTimestamp(LocalDateTime.now());
        }

        Long roomId = message.getRoomId();
        if (roomId == null) {
            throw new IllegalArgumentException("Room ID cannot be null");
        }

        // 이벤트 발행
        MessageEvent event = new MessageEvent("SEND", message);

        // Kafka로 메시지 전송
        kafkaTemplate.send(MESSAGE_TOPIC, null, event);

        logger.info("Message with ID: {} sent successfully to Kafka topic: {}", message.getId(), MESSAGE_TOPIC);
    }

    /**
     * 메시지 삭제
     */
    public void deleteMessage(String messageId) {
        Message message = messageRepository.findById(messageId).orElse(null);
        if (message != null) {
            messageRepository.delete(message);
            // 이벤트 발행
            MessageEvent event = new MessageEvent("DELETE", message);
            kafkaTemplate.send(MESSAGE_TOPIC, null, event);
            logger.info("Message with ID: {} deleted and event sent to Kafka.", messageId);
        } else {
            logger.warn("Message with ID: {} not found.", messageId);
        }
    }

    /**
     * 메시지 이벤트 소비
     */
    @KafkaListener(
            topics = "message-events",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeMessageEvent(MessageEvent event) {
        if ("SEND".equals(event.getAction())) {
            Message message = event.getMessage();
            saveMessage(message); // 메시지 저장
        } else if ("DELETE".equals(event.getAction())) {
            Message message = event.getMessage();
            messageRepository.deleteById(message.getId());
            logger.info("Message with ID: {} deleted from the database.", message.getId());
        }
    }

    /**
     * 특정 채팅방의 메시지 조회
     */
    // @Cacheable(value = "message::messages", key = "#roomId + '-' + #page + '-' + #size")
    public Page<Message> getMessagesByRoomId(Long roomId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        logger.info("Fetching messages for roomId: {}, page: {}, size: {}", roomId, page, size);
        return messageRepository.findByRoomId(roomId, pageRequest);
    }
}
