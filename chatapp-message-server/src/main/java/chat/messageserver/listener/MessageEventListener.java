package chat.messageserver.listener;

import chat.messageserver.event.MessageEvent;
import chat.messageserver.model.Message;
import chat.messageserver.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Kafka에서 수신한 메시지 이벤트를 처리하는 리스너 클래스.
 */
@Component
public class MessageEventListener {

    private static final Logger logger = LoggerFactory.getLogger(MessageEventListener.class);

    private final MessageService messageService;

    /**
     * 생성자 주입을 통해 의존성을 주입받음.
     *
     * @param messageService 메시지 서비스
     */
    public MessageEventListener(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * Kafka에서 메시지 이벤트를 소비함.
     *
     * @param event 소비한 메시지 이벤트
     */
    @KafkaListener(
            topics = "message-events",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consumeMessageEvent(MessageEvent event) {
        String action = event.getAction();
        Message message = event.getMessage();

        if ("SEND".equalsIgnoreCase(action)) {
            logger.info("Consuming SEND event for message ID: {}", message.getId());
            messageService.saveMessage(message); // 메시지 저장
        } else if ("DELETE".equalsIgnoreCase(action)) {
            logger.info("Consuming DELETE event for message ID: {}", message.getId());
            messageService.deleteMessage(message.getId());
        } else {
            logger.warn("Received unknown action type: {}", action);
        }
    }
}

