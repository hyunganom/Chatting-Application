package chat.websocketserver.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
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

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 생성자 주입을 통해 RedisTemplate을 주입받음.
     *
     * @param redisTemplate Redis 템플릿
     */
    public ChatRoomUserService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 유저를 특정 채팅방에 추가함.
     *
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     */
    public void addUserToChatRoom(Long roomId, Long userId) {
        if (roomId == null || userId == null) {
            logger.warn("Attempted to add user with null roomId or userId. roomId: {}, userId: {}", roomId, userId);
            return;
        }

        String key = generateChatRoomUsersKey(roomId);
        try {
            // 유저 ID를 문자열로 변환하여 저장
            redisTemplate.opsForSet().add(key, userId.toString());
            logger.info("User ID: {} added to ChatRoom ID: {}", userId, roomId);
        } catch (DataAccessException e) {
            logger.error("Failed to add User ID: {} to ChatRoom ID: {}. Error: {}", userId, roomId, e.getMessage(), e);
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
        if (roomId == null || userId == null) {
            logger.warn("Attempted to remove user with null roomId or userId. roomId: {}, userId: {}", roomId, userId);
            return;
        }

        String key = generateChatRoomUsersKey(roomId);
        try {
            // 유저 ID를 문자열로 변환하여 제거
            redisTemplate.opsForSet().remove(key, userId.toString());
            logger.info("User ID: {} removed from ChatRoom ID: {}", userId, roomId);
        } catch (DataAccessException e) {
            logger.error("Failed to remove User ID: {} from ChatRoom ID: {}. Error: {}", userId, roomId, e.getMessage(), e);
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
        if (roomId == null) {
            logger.warn("Attempted to get users with null roomId.");
            return Collections.emptySet();
        }

        String key = generateChatRoomUsersKey(roomId);
        try {
            Set<Object> userIds = redisTemplate.opsForSet().members(key);
            if (userIds != null && !userIds.isEmpty()) {
                Set<Long> longUserIds = userIds.stream()
                        .map(Object::toString)
                        .filter(this::isNumeric)
                        .map(Long::valueOf)
                        .collect(Collectors.toSet());
                logger.info("Retrieved {} users from ChatRoom ID: {}", longUserIds.size(), roomId);
                return longUserIds;
            }
            logger.info("No users found in ChatRoom ID: {}", roomId);
            return Collections.emptySet();
        } catch (DataAccessException e) {
            logger.error("Failed to retrieve users from ChatRoom ID: {}. Error: {}", roomId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 채팅방 사용자 키를 생성함.
     *
     * @param roomId 채팅방 ID
     * @return Redis 키
     */
    private String generateChatRoomUsersKey(Long roomId) {
        return CHATROOM_USERS_KEY_PREFIX + roomId;
    }

    /**
     * 문자열이 숫자인지 확인함.
     *
     * @param str 검사할 문자열
     * @return 숫자 여부
     */
    private boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
