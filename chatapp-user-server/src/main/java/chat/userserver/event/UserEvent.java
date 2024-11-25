package chat.userserver.event;

import chat.userserver.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent {
    private String action; // CREATE, UPDATE, DELETE
    private User user;
}
