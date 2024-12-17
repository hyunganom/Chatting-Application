package chat.websocketserver.model;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Message implements Serializable {
    private String id;
    private Long roomId;
    private Long userId;
    private String content;
    private String sender;
    private LocalDateTime timestamp;
}
