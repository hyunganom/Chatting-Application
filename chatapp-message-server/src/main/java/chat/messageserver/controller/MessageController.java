package chat.messageserver.controller;

import chat.messageserver.model.Message;
import chat.messageserver.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/messages")
@CrossOrigin
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private MessageService messageService;

    /**
     * 메시지 전송
     *
     * @RequestBody Message  채팅방 ID
     * @return 응답 상태
     */
    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody Message message) {
        logger.info("Received request to send message : {}", message);
        messageService.saveMessage(message);
        return ResponseEntity.ok("Message sent successfully!");
    }

    /**
     * 채팅방의 모든 메시지 조회
     *
     * @param roomId 채팅방 ID
     * @return 메시지 리스트
     */
    @GetMapping("/room/{roomId}")
    public ResponseEntity<Page<Message>> getMessages(@PathVariable Long roomId,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        Page<Message> messages = messageService.getMessagesByRoomId(roomId, page, size);
        return ResponseEntity.ok(messages);
    }

    /**
     * 메시지 삭제
     *
     * @param messageId 메시지 ID
     * @return 응답 상태
     */
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable String messageId) {
        messageService.deleteMessage(messageId);
        return ResponseEntity.noContent().build();
    }
}
