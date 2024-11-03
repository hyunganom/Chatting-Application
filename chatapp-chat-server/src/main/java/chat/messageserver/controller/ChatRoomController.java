package chat.messageserver.controller;

import chat.messageserver.model.ChatRoom;
import chat.messageserver.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chatrooms")
public class ChatRoomController {

    @Autowired
    private ChatRoomService chatRoomService;

    @PostMapping("/create")
    public ResponseEntity<ChatRoom> createRoom(@RequestParam String roomName) {
        ChatRoom chatRoom = chatRoomService.createRoom(roomName);
        return ResponseEntity.ok(chatRoom);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ChatRoom> getChatRoom(@PathVariable Long roomId) {
        ChatRoom chatRoom = chatRoomService.getChatRoomById(roomId);
        if (chatRoom != null) {
            return ResponseEntity.ok(chatRoom);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

