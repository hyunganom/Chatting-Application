package chat.websocketserver.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ChatRoomUserService {

    private static final String CHATROOM_USERS_KEY_PREFIX = "chatroom:users:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 유저를 채팅방에 추가
     */
    public void addUserToChatRoom(Long roomId, Long userId) {
        String key = CHATROOM_USERS_KEY_PREFIX + roomId;
        redisTemplate.opsForSet().add(key, userId);
    }

    /**
     * 유저를 채팅방에서 제거
     */
    public void removeUserFromChatRoom(Long roomId, Long userId) {
        String key = CHATROOM_USERS_KEY_PREFIX + roomId;
        redisTemplate.opsForSet().remove(key, userId);
    }

    /**
     * 채팅방에 현재 접속한 유저 ID 목록 조회
     */
    public Set<Long> getUsersInChatRoom(Long roomId) {
        String key = CHATROOM_USERS_KEY_PREFIX + roomId;
        Set<Object> userIds = redisTemplate.opsForSet().members(key);
        if (userIds != null) {
            return userIds.stream().map(id -> Long.parseLong(id.toString())).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }
}
