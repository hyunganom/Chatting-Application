package chat.userserver.event;

import chat.userserver.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사용자 이벤트를 나타내는 클래스.
 * 이벤트는 CREATE, UPDATE, DELETE 작업을 포함.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent {

    // 이벤트 종류를 상수로 정의하여 일관성 유지
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";

    /**
     * 이벤트의 종류.
     */
    private String action;

    /**
     * 이벤트와 관련된 사용자 정보.
     */
    private User user;
}
