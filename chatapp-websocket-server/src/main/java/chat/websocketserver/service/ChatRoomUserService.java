package chat.websocketserver.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 채팅방 내 사용자 관리를 위한 서비스 클래스.
 * Redis를 사용하여 사용자 목록을 관리함.
 */
@Service
public class ChatRoomUserService {

    private static final Logger logger = LoggerFactory.getLogger(ChatRoomUserService.class);
    private static final String CHATROOM_USERS_KEY_PREFIX = "chatroom:users:";

    private final RedisTemplate<String, Long> redisTemplate;

    /**
     * 생성자 주입을 통해 의존성을 주입받음.
     *
     * @param redisTemplate Redis 템플릿
     */
    public ChatRoomUserService(RedisTemplate<String, Long> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 유저를 특정 채팅방에 추가함.
     *
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     */
    public void addUserToChatRoom(Long roomId, Long userId) {
        String key = CHATROOM_USERS_KEY_PREFIX + roomId;
        try {
            redisTemplate.opsForSet().add(key, userId);
            logger.info("User ID: {} added to ChatRoom ID: {}", userId, roomId);
        } catch (DataAccessException e) {
            logger.error("Failed to add User ID: {} to ChatRoom ID: {}. Error: {}", userId, roomId, e.getMessage());
            throw e;
        }
    }

    /**
     * 유저를 특정 채팅방에서 제거함.
     *
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     */
    public void removeUserFromChatRoom(Long roomId, Long userId) {
        String key = CHATROOM_USERS_KEY_PREFIX + roomId;
        try {
            redisTemplate.opsForSet().remove(key, userId);
            logger.info("User ID: {} removed from ChatRoom ID: {}", userId, roomId);
        } catch (DataAccessException e) {
            logger.error("Failed to remove User ID: {} from ChatRoom ID: {}. Error: {}", userId, roomId, e.getMessage());
            throw e;
        }
    }

    /**
     * 특정 채팅방에 현재 접속한 유저 ID 목록을 조회함.
     *
     * @param roomId 채팅방 ID
     * @return 유저 ID의 Set, 없을 경우 빈 Set 반환
     */
    public Set<Long> getUsersInChatRoom(Long roomId) {
        String key = CHATROOM_USERS_KEY_PREFIX + roomId;
        try {
            Set<Long> userIds = redisTemplate.opsForSet().members(key);
            if (userIds != null) {
                logger.info("Retrieved {} users from ChatRoom ID: {}", userIds.size(), roomId);
                return userIds;
            }
            logger.info("No users found in ChatRoom ID: {}", roomId);
            return Collections.emptySet();
        } catch (DataAccessException e) {
            logger.error("Failed to retrieve users from ChatRoom ID: {}. Error: {}", roomId, e.getMessage());
            throw e;
        }
    }
}
