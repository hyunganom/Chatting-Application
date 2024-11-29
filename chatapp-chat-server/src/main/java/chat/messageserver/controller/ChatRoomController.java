package chat.messageserver.controller;

import chat.messageserver.model.ChatRoom;
import chat.messageserver.service.ChatRoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * 채팅방 관련 REST API를 제공하는 컨트롤러 클래스.
 */
@RestController
@RequestMapping("/chatrooms")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    /**
     * 생성자 주입을 통해 ChatRoomService를 주입받음.
     *
     * @param chatRoomService 채팅방 서비스
     */
    public ChatRoomController(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }

    /**
     * 새로운 채팅방을 생성함.
     *
     * @param roomName 생성할 채팅방 이름
     * @return 생성된 채팅방 정보와 201 Created 상태
     */
    @PostMapping("/create")
    public ResponseEntity<ChatRoom> createRoom(@RequestParam String roomName) {
        ChatRoom chatRoom = chatRoomService.createRoom(roomName);
        URI location = URI.create("/chatrooms/" + chatRoom.getId());
        return ResponseEntity.created(location).body(chatRoom);
    }

    /**
     * 특정 ID를 가진 채팅방을 조회함.
     *
     * @param roomId 조회할 채팅방 ID
     * @return 채팅방 정보와 200 OK 상태, 또는 404 Not Found 상태
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<ChatRoom> getChatRoom(@PathVariable Long roomId) {
        ChatRoom chatRoom = chatRoomService.getChatRoomById(roomId);
        return chatRoom != null ? ResponseEntity.ok(chatRoom) : ResponseEntity.notFound().build();
    }

    /**
     * 기존 채팅방의 이름을 업데이트함.
     *
     * @param roomId  업데이트할 채팅방 ID
     * @param newName 새로운 채팅방 이름
     * @return 업데이트된 채팅방 정보와 200 OK 상태
     */
    @PutMapping("/{roomId}")
    public ResponseEntity<ChatRoom> updateRoom(@PathVariable Long roomId, @RequestParam String newName) {
        ChatRoom updatedRoom = chatRoomService.updateRoom(roomId, newName);
        return ResponseEntity.ok(updatedRoom);
    }

    /**
     * 특정 ID를 가진 채팅방을 삭제함.
     *
     * @param roomId 삭제할 채팅방 ID
     * @return 204 No Content 상태
     */
    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        chatRoomService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 모든 채팅방을 조회함.
     *
     * @return 채팅방 리스트와 200 OK 상태
     */
    @GetMapping("/all")
    public ResponseEntity<List<ChatRoom>> getAllChatRooms() {
        List<ChatRoom> chatRooms = chatRoomService.getAllChatRooms();
        return ResponseEntity.ok(chatRooms);
    }
}
