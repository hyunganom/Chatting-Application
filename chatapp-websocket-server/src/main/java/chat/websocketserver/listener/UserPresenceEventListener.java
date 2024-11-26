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

@Service
public class UserPresenceEventListener {

    @Autowired
    private ChatRoomUserService chatRoomUserService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserService userService;

    @KafkaListener(
            topics = "user-presence-events",
            groupId = "user_presence_group",
            containerFactory = "userPresenceKafkaListenerContainerFactory"
    )
    public void consumeUserPresenceEvent(UserPresenceEvent event) {
        Long roomId = event.getRoomId();
        Long userId = event.getUserId();

        if ("JOIN".equals(event.getAction())) {
            chatRoomUserService.addUserToChatRoom(roomId, userId);
        } else if ("LEAVE".equals(event.getAction())) {
            chatRoomUserService.removeUserFromChatRoom(roomId, userId);
        }

        // 유저 목록 브로드캐스트
        broadcastUserList(roomId);
    }

    private void broadcastUserList(Long roomId) {
        Set<Long> userIds = chatRoomUserService.getUsersInChatRoom(roomId);
        List<User> users = userService.getUsersByIds(userIds);
        messagingTemplate.convertAndSend("/topic/chatroom-" + roomId + "-users", users);
    }
}
