package chat.messageserver.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Document(collection = "messages")
public class Message implements Serializable {

    @Id
    private String id;

    private Long userId;

    private Long roomId;
    private String sender;
    private String content;
    private LocalDateTime timestamp;

    public Message() {}

    public Message(Long roomId, String sender, String content, LocalDateTime timestamp) {
        this.roomId = roomId;
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }
}