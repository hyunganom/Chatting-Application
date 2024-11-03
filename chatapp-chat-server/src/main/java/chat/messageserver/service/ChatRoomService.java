package chat.messageserver.service;

import chat.messageserver.model.ChatRoom;
import chat.messageserver.repository.ChatRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ChatRoomService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Cacheable(value = "chatRooms", key = "#roomId")
    public ChatRoom getChatRoomById(Long roomId) {
        return chatRoomRepository.findById(roomId).orElse(null);
    }

    public ChatRoom createRoom(String roomName) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setRoomName(roomName);
        return chatRoomRepository.save(chatRoom);
    }
}

