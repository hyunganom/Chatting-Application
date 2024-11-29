package chat.websocketserver.listener;

import chat.websocketserver.event.UserPresenceEvent;
import chat.websocketserver.model.User;
import chat.websocketserver.service.ChatRoomUserService;
import chat.websocketserver.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class UserPresenceEventListener {

    private static final String USER_PRESENCE_TOPIC = "user-presence-events";

    private final ChatRoomUserService chatRoomUserService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    /**
     * 생성자 주입을 통해 의존성을 주입받음.
     *
     * @param chatRoomUserService 채팅방 사용자 서비스
     * @param messagingTemplate    메시징 템플릿
     * @param userService          사용자 서비스
     */
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
     * @param event 소비한 사용자 존재 이벤트
     */
    @KafkaListener(
            topics = USER_PRESENCE_TOPIC,
            groupId = "user_presence_group",
            containerFactory = "userPresenceKafkaListenerContainerFactory"
    )
    public void consumeUserPresenceEvent(UserPresenceEvent event) {
        Long roomId = event.getRoomId();
        Long userId = event.getUserId();
        String action = event.getAction();

        try {
            if ("JOIN".equalsIgnoreCase(action)) {
                chatRoomUserService.addUserToChatRoom(roomId, userId);
                log.info("User ID: {} joined ChatRoom ID: {}", userId, roomId);
            } else if ("LEAVE".equalsIgnoreCase(action)) {
                chatRoomUserService.removeUserFromChatRoom(roomId, userId);
                log.info("User ID: {} left ChatRoom ID: {}", userId, roomId);
            } else {
                log.warn("Received unknown action type: {}", action);
            }

            // 유저 목록 브로드캐스트
            broadcastUserList(roomId);
        } catch (Exception e) {
            log.error("Error processing UserPresenceEvent: {}", event, e);
        }
    }

    /**
     * 특정 채팅방의 현재 접속한 유저 목록을 브로드캐스트함.
     *
     * @param roomId 채팅방 ID
     */
    private void broadcastUserList(Long roomId) {
        try {
            Set<Long> userIds = chatRoomUserService.getUsersInChatRoom(roomId);
            List<User> users = userService.getUsersByIds(userIds);
            messagingTemplate.convertAndSend("/topic/chatroom-" + roomId + "-users", users);
            log.info("Broadcasted user list for ChatRoom ID: {}", roomId);
        } catch (Exception e) {
            log.error("Failed to broadcast user list for ChatRoom ID: {}", roomId, e);
        }
    }
}
