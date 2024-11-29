package chat.websocketserver.listener;

import chat.websocketserver.event.UserPresenceEvent;
import chat.websocketserver.model.User;
import chat.websocketserver.service.ChatRoomUserService;
import chat.websocketserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 사용자 존재 이벤트를 처리하는 리스너.
 */
@Service
public class UserPresenceEventListener {

    private final ChatRoomUserService chatRoomUserService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    private static final String ACTION_JOIN = "JOIN";
    private static final String ACTION_LEAVE = "LEAVE";

    @Autowired
    public UserPresenceEventListener(ChatRoomUserService chatRoomUserService,
                                     SimpMessagingTemplate messagingTemplate,
                                     UserService userService) {
        this.chatRoomUserService = chatRoomUserService;
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
    }

    /**
     * Kafka에서 사용자 존재 이벤트를 소비함.
     *
     * @param event 소비된 사용자 존재 이벤트
     */
    @KafkaListener(
            topics = "user-presence-events",
            groupId = "user_presence_group",
            containerFactory = "userPresenceKafkaListenerContainerFactory"
    )
    public void consumeUserPresenceEvent(UserPresenceEvent event) {
        Long roomId = event.getRoomId();
        Long userId = event.getUserId();

        if (ACTION_JOIN.equals(event.getAction())) {
            chatRoomUserService.addUserToChatRoom(roomId, userId);
        } else if (ACTION_LEAVE.equals(event.getAction())) {
            chatRoomUserService.removeUserFromChatRoom(roomId, userId);
        }

        // 유저 목록을 브로드캐스트함.
        broadcastUserList(roomId);
    }

    /**
     * 특정 채팅방의 현재 유저 목록을 브로드캐스트함.
     *
     * @param roomId 채팅방 ID
     */
    private void broadcastUserList(Long roomId) {
        Set<Long> userIds = chatRoomUserService.getUsersInChatRoom(roomId);
        List<User> users = userService.getUsersByIds(userIds);
        messagingTemplate.convertAndSend("/topic/chatroom-" + roomId + "-users", users);
    }
}
