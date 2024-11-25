package chat.messageserver.service;

import chat.messageserver.event.ChatRoomEvent;
import chat.messageserver.exception.ResourceNotFoundException;
import chat.messageserver.model.ChatRoom;
import chat.messageserver.repository.ChatRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatRoomService {


    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private KafkaTemplate<String, ChatRoomEvent> kafkaTemplate;

    private static final String CHAT_ROOM_TOPIC = "chatroom-events";

    @Cacheable(value = "chat::chatRooms", key = "#roomId")
    public ChatRoom getChatRoomById(Long roomId) {
        return chatRoomRepository.findById(roomId).orElse(null);
    }

    public ChatRoom createRoom(String roomName) {
        // 채팅방 이름 중복 확인
        if (chatRoomRepository.findAll().stream().anyMatch(room -> room.getRoomName().equals(roomName))) {
            throw new IllegalArgumentException("Chat room name already exists");
        }

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setRoomName(roomName);
        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

        // 이벤트 발행
        ChatRoomEvent event = new ChatRoomEvent("CREATE", savedRoom);
        kafkaTemplate.send(CHAT_ROOM_TOPIC, event);

        return savedRoom;
    }

    public ChatRoom updateRoom(Long roomId, String newName) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom not found"));
        chatRoom.setRoomName(newName);
        ChatRoom updatedRoom = chatRoomRepository.save(chatRoom);

        // 이벤트 발행
        ChatRoomEvent event = new ChatRoomEvent("UPDATE", updatedRoom);
        kafkaTemplate.send(CHAT_ROOM_TOPIC, event);

        return updatedRoom;
    }

    public void deleteRoom(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom not found"));
        chatRoomRepository.delete(chatRoom);

        // 이벤트 발행
        ChatRoomEvent event = new ChatRoomEvent("DELETE", chatRoom);
        kafkaTemplate.send(CHAT_ROOM_TOPIC, event);
    }

    public List<ChatRoom> getAllChatRooms() {
        return chatRoomRepository.findAll();
    }

}

