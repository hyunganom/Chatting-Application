package chat.messageserver.service;

import chat.messageserver.model.Message;
import chat.messageserver.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.UUID;

@Service
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);
    private static final String TOPIC_PREFIX = "chatroom-";

    @Autowired
    private KafkaTemplate<String, Message> kafkaTemplate;

    @Autowired
    private MessageRepository messageRepository;

    public void saveMessage(String roomId, Message message) {
        message.setRoomId(roomId);
        message.setId(UUID.randomUUID().toString());
        message.setTimestamp(System.currentTimeMillis());

        logger.info("Processing message with ID: {} for room ID: {}", message.getId(), roomId);

        // 메시지를 데이터베이스에 저장
        try {
            messageRepository.save(message);
            logger.info("Message with ID: {} saved successfully to the database.", message.getId());
        } catch (Exception e) {
            logger.error("Error saving message with ID: {} to the database: {}", message.getId(), e.getMessage());
        }
    }

    public void sendMessage(String roomId, Message message) {
        message.setRoomId(roomId);
        message.setId(UUID.randomUUID().toString());
        message.setTimestamp(System.currentTimeMillis());

        logger.info("Sending message with ID: {} to Kafka topic: {}", message.getId(), TOPIC_PREFIX + roomId);

        // Partition key를 null로 설정하여 라운드 로빈 방식으로 파티션에 분배
        ListenableFuture<SendResult<String, Message>> future = kafkaTemplate.send(TOPIC_PREFIX + roomId, null, message);

        future.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onSuccess(SendResult<String, Message> result) {
                logger.info("Message with ID: {} sent successfully to Kafka topic: {}", message.getId(), result.getProducerRecord().topic());
            }

            @Override
            public void onFailure(Throwable ex) {
                logger.error("Error sending message with ID: {} to Kafka topic: {}. Error: {}", message.getId(), TOPIC_PREFIX + roomId, ex.getMessage());
            }
        });

        try {
            messageRepository.save(message);
            logger.info("Message with ID: {} saved successfully to the database.", message.getId());
        } catch (Exception e) {
            logger.error("Error saving message with ID: {} to the database: {}", message.getId(), e.getMessage());
        }
    }


    public Page<Message> getMessagesByRoomId(String roomId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        logger.info("Fetching messages for roomId: {}, page: {}, size: {}", roomId, page, size);
        return messageRepository.findByRoomId(roomId, pageRequest);
    }
}
