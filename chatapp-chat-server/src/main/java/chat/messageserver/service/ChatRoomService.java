package chat.messageserver.service;

import chat.messageserver.event.ChatRoomEvent;
import chat.messageserver.event.ChatRoomEvent.ActionType;
import chat.messageserver.exception.DuplicateResourceException;
import chat.messageserver.exception.ResourceNotFoundException;
import chat.messageserver.model.ChatRoom;
import chat.messageserver.repository.ChatRoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 채팅방 관련 비즈니스 로직을 처리하는 서비스 클래스.
 */
@Service
public class ChatRoomService {

    private static final Logger logger = LoggerFactory.getLogger(ChatRoomService.class);
    private static final String CHAT_ROOM_TOPIC = "chatroom-events";

    private final ChatRoomRepository chatRoomRepository;
    private final KafkaTemplate<String, ChatRoomEvent> kafkaTemplate;

    /**
     * 생성자 주입을 통해 의존성을 주입받음.
     *
     * @param chatRoomRepository 채팅방 리포지토리
     * @param kafkaTemplate      Kafka 템플릿
     */
    public ChatRoomService(ChatRoomRepository chatRoomRepository,
                           KafkaTemplate<String, ChatRoomEvent> kafkaTemplate) {
        this.chatRoomRepository = chatRoomRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * ID로 채팅방을 조회함. 캐시를 활용하여 조회 성능을 향상시킴.
     *
     * @param roomId 채팅방 ID
     * @return 채팅방 객체
     */
    @Cacheable(value = "chat::chatRooms", key = "#roomId")
    public ChatRoom getChatRoomById(Long roomId) {
        logger.info("Fetching ChatRoom with ID: {}", roomId);
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom not found with ID: " + roomId));
    }

    /**
     * 새로운 채팅방을 생성함.
     *
     * @param roomName 채팅방 이름
     * @return 생성된 채팅방 객체
     */
    @Transactional
    @CacheEvict(value = "chat::chatRooms", allEntries = true)
    public ChatRoom createRoom(String roomName) {
        logger.info("Creating ChatRoom with name: {}", roomName);

        validateRoomName(roomName);

        // 채팅방 이름 중복 확인
        if (chatRoomRepository.existsByRoomName(roomName)) {
            logger.error("ChatRoom name already exists: {}", roomName);
            throw new DuplicateResourceException("Chat room name already exists: " + roomName);
        }

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setRoomName(roomName);
        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

        // 이벤트 발행
        publishEvent(ActionType.CREATE, savedRoom);

        logger.info("ChatRoom created successfully: {}", savedRoom);
        return savedRoom;
    }

    /**
     * 기존 채팅방의 이름을 업데이트함.
     *
     * @param roomId  채팅방 ID
     * @param newName 새로운 채팅방 이름
     * @return 업데이트된 채팅방 객체
     */
    @Transactional
    @CacheEvict(value = "chat::chatRooms", key = "#roomId")
    public ChatRoom updateRoom(Long roomId, String newName) {
        logger.info("Updating ChatRoom with ID: {} to new name: {}", roomId, newName);

        validateRoomName(newName);

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom not found with ID: " + roomId));

        // 채팅방 이름 중복 확인
        if (chatRoomRepository.existsByRoomName(newName)) {
            logger.error("ChatRoom name already exists: {}", newName);
            throw new DuplicateResourceException("Chat room name already exists: " + newName);
        }

        chatRoom.setRoomName(newName);
        ChatRoom updatedRoom = chatRoomRepository.save(chatRoom);

        // 이벤트 발행
        publishEvent(ActionType.UPDATE, updatedRoom);

        logger.info("ChatRoom updated successfully: {}", updatedRoom);
        return updatedRoom;
    }

    /**
     * 채팅방을 삭제함.
     *
     * @param roomId 채팅방 ID
     */
    @Transactional
    @CacheEvict(value = "chat::chatRooms", key = "#roomId")
    public void deleteRoom(Long roomId) {
        logger.info("Deleting ChatRoom with ID: {}", roomId);

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom not found with ID: " + roomId));

        chatRoomRepository.delete(chatRoom);

        // 이벤트 발행
        publishEvent(ActionType.DELETE, chatRoom);

        logger.info("ChatRoom deleted successfully: {}", chatRoom);
    }

    /**
     * 모든 채팅방을 조회함.
     *
     * @return 채팅방 리스트
     */
    public List<ChatRoom> getAllChatRooms() {
        logger.info("Fetching all ChatRooms");
        return chatRoomRepository.findAll();
    }

    /**
     * 채팅방 이름의 유효성을 검증함.
     *
     * @param roomName 채팅방 이름
     */
    private void validateRoomName(String roomName) {
        if (roomName == null || roomName.trim().isEmpty()) {
            logger.error("Invalid ChatRoom name: '{}'", roomName);
            throw new IllegalArgumentException("Chat room name must not be null or empty");
        }
    }

    /**
     * Kafka를 통해 채팅방 이벤트를 발행함.
     *
     * @param action    이벤트 유형
     * @param chatRoom  관련된 채팅방 정보
     */
    private void publishEvent(ActionType action, ChatRoom chatRoom) {
        ChatRoomEvent event = new ChatRoomEvent(action, chatRoom);
        kafkaTemplate.send(CHAT_ROOM_TOPIC, event);
        logger.debug("Published event to Kafka: {}", event);
    }
}
