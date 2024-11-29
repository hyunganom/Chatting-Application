package chat.messageserver.service;

import chat.messageserver.event.MessageEvent;
import chat.messageserver.model.Message;
import chat.messageserver.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.time.LocalDateTime;

/**
 * 메시지 관련 비즈니스 로직을 처리하는 서비스 클래스.
 */
@Service
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);
    private static final String MESSAGE_TOPIC = "message-events";

    private final MessageRepository messageRepository;
    private final KafkaTemplate<String, MessageEvent> kafkaTemplate;

    /**
     * 생성자 주입을 통해 의존성을 주입받음.
     *
     * @param messageRepository 메시지 리포지토리
     * @param kafkaTemplate     Kafka 템플릿
     */
    public MessageService(MessageRepository messageRepository,
                          KafkaTemplate<String, MessageEvent> kafkaTemplate) {
        this.messageRepository = messageRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * 메시지를 저장함.
     *
     * @param message 저장할 메시지 객체
     */
    @Transactional
    public void saveMessage(Message message) {
        // 필수 필드 설정 및 검증
        if (message.getId() == null) {
            // ID가 Long 타입이라면 UUID 대신 적절한 방식으로 설정해야 함
            // 예: message.setId(generateUniqueId());
        }
        if (message.getTimestamp() == null) {
            message.setTimestamp(LocalDateTime.now());
        }

        try {
            messageRepository.save(message);
            logger.info("Message with ID: {} saved successfully to the database.", message.getId());
        } catch (Exception e) {
            logger.error("Error saving message with ID: {} to the database: {}", message.getId(), e.getMessage());
            throw e;
        }
    }

    /**
     * 메시지를 전송함.
     *
     * @param message 전송할 메시지 객체
     */
    @Transactional
    public void sendMessage(Message message) {
        // 필수 필드 설정 및 검증
        if (message.getId() == null) {
            // ID가 Long 타입이라면 UUID 대신 적절한 방식으로 설정해야 함
            // 예: message.setId(generateUniqueId());
        }
        if (message.getTimestamp() == null) {
            message.setTimestamp(LocalDateTime.now());
        }

        Long roomId = message.getRoomId();
        if (roomId == null) {
            logger.error("Room ID cannot be null for message with ID: {}", message.getId());
            throw new IllegalArgumentException("Room ID cannot be null");
        }

        // 이벤트 발행
        MessageEvent event = new MessageEvent("SEND", message);

        // Kafka로 메시지 전송
        kafkaTemplate.send(MESSAGE_TOPIC, null, event)
                .addCallback(new ListenableFutureCallback<SendResult<String, MessageEvent>>() {
                    @Override
                    public void onSuccess(SendResult<String, MessageEvent> result) {
                        logger.info("Message with ID: {} sent successfully to Kafka topic: {}", message.getId(), MESSAGE_TOPIC);
                    }

                    @Override
                    public void onFailure(Throwable ex) {
                        logger.error("Failed to send message with ID: {} to Kafka topic: {}. Error: {}", message.getId(), MESSAGE_TOPIC, ex.getMessage());
                        // 필요 시 재시도 로직 추가 가능
                    }
                });
    }

    /**
     * 메시지를 삭제함.
     *
     * @param messageId 삭제할 메시지 ID
     */
    @Transactional
    public void deleteMessage(String messageId) {
        Message message = messageRepository.findById(messageId).orElse(null);
        if (message != null) {
            messageRepository.delete(message);
            // 이벤트 발행
            MessageEvent event = new MessageEvent("DELETE", message);
            kafkaTemplate.send(MESSAGE_TOPIC, null, event)
                    .addCallback(new ListenableFutureCallback<SendResult<String, MessageEvent>>() {
                        @Override
                        public void onSuccess(SendResult<String, MessageEvent> result) {
                            logger.info("Message with ID: {} deleted and event sent to Kafka.", messageId);
                        }

                        @Override
                        public void onFailure(Throwable ex) {
                            logger.error("Failed to send DELETE event for message ID: {} to Kafka. Error: {}", messageId, ex.getMessage());
                            // 필요 시 재시도 로직 추가 가능
                        }
                    });
        } else {
            logger.warn("Message with ID: {} not found.", messageId);
        }
    }

    /**
     * 특정 채팅방의 메시지를 페이지네이션하여 조회함.
     *
     * @param roomId 채팅방 ID
     * @param page   페이지 번호 (0부터 시작)
     * @param size   페이지당 메시지 수
     * @return 메시지 페이지
     */
    @Transactional(readOnly = true)
    public Page<Message> getMessagesByRoomId(Long roomId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        logger.info("Fetching messages for roomId: {}, page: {}, size: {}", roomId, page, size);
        return messageRepository.findByRoomId(roomId, pageRequest);
    }
}
