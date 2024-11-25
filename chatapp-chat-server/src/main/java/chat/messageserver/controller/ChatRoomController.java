package chat.messageserver.controller;

import chat.messageserver.model.ChatRoom;
import chat.messageserver.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/chatrooms")
public class ChatRoomController {

    @Autowired
    private ChatRoomService chatRoomService;

//    @PostMapping("/create")
//    public ResponseEntity<ChatRoom> createRoom(@RequestParam String roomName) {
//        ChatRoom chatRoom = chatRoomService.createRoom(roomName);
//        return ResponseEntity.ok(chatRoom);
//    }
//
//    @GetMapping("/{roomId}")
//    public ResponseEntity<ChatRoom> getChatRoom(@PathVariable Long roomId) {
//        ChatRoom chatRoom = chatRoomService.getChatRoomById(roomId);
//        if (chatRoom != null) {
//            return ResponseEntity.ok(chatRoom);
//        } else {
//            return ResponseEntity.notFound().build();
//        }
//    }

    /**
     * 채팅방 생성
     *
     * @param roomName 채팅방 이름
     * @return 생성된 채팅방 정보
     */
    @PostMapping("/create")
    public ResponseEntity<ChatRoom> createRoom(@RequestParam String roomName) {
        ChatRoom chatRoom = chatRoomService.createRoom(roomName);
        return ResponseEntity.created(URI.create("/chatrooms/" + chatRoom.getId())).body(chatRoom);
    }

    /**
     * 채팅방 조회
     *
     * @param roomId 채팅방 ID
     * @return 채팅방 정보
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<ChatRoom> getChatRoom(@PathVariable Long roomId) {
        ChatRoom chatRoom = chatRoomService.getChatRoomById(roomId);
        if (chatRoom != null) {
            return ResponseEntity.ok(chatRoom);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 채팅방 업데이트
     *
     * @param roomId  채팅방 ID
     * @param newName 새로운 채팅방 이름
     * @return 업데이트된 채팅방 정보
     */
    @PutMapping("/{roomId}")
    public ResponseEntity<ChatRoom> updateRoom(@PathVariable Long roomId, @RequestParam String newName) {
        ChatRoom updatedRoom = chatRoomService.updateRoom(roomId, newName);
        return ResponseEntity.ok(updatedRoom);
    }

    /**
     * 채팅방 삭제
     *
     * @param roomId 채팅방 ID
     * @return 응답 상태
     */
    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        chatRoomService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 모든 채팅방 조회
     *
     * @return 채팅방 리스트
     */
    @GetMapping("/all")
    public ResponseEntity<List<ChatRoom>> getAllChatRooms() {
        List<ChatRoom> chatRooms = chatRoomService.getAllChatRooms();
        return ResponseEntity.ok(chatRooms);
    }
}

