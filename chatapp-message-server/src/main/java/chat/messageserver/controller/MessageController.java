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
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private MessageService messageService;

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestParam String roomId, @RequestBody Message message) {
        logger.info("Received request to send message to roomId: {}", roomId);
        messageService.saveMessage(roomId, message);
        return ResponseEntity.ok("Message sent successfully!");
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<Page<Message>> getMessages(@PathVariable String roomId,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        logger.info("Received request to fetch messages for roomId: {}", roomId);
        Page<Message> messages = messageService.getMessagesByRoomId(roomId, page, size);
        return ResponseEntity.ok(messages);
    }
}
