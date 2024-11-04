package chat.websocketserver.model;

import lombok.Data;

@Data
public class Message {

    private String id;
    private String roomId;
    private String sender;
    private String content;
    private long timestamp;

    public Message() {}

    public Message(String roomId, String sender, String content, long timestamp) {
        this.roomId = roomId;
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }
}
