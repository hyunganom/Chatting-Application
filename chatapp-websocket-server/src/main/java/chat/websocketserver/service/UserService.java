package chat.websocketserver.service;

import chat.websocketserver.client.UserServiceClient;
import chat.websocketserver.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 유저 관련 비즈니스 로직을 처리하는 서비스 클래스.
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserServiceClient userServiceClient;

    /**
     * 생성자 주입을 통해 의존성을 주입받음.
     *
     * @param userServiceClient 외부 유저 서비스 클라이언트
     */
    public UserService(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    /**
     * 유저 ID 목록을 받아 유저 정보 리스트를 반환함.
     *
     * @param userIds 유저 ID의 집합
     * @return 유저 정보 리스트
     */
    public List<User> getUsersByIds(Set<Long> userIds) {
        if (userIds.isEmpty()) {
            logger.info("Received empty userIds set. Returning empty user list.");
            return List.of();
        }

        try {
            logger.info("Fetching users for userIds: {}", userIds);
            List<User> users = userServiceClient.getUsersByIds(List.copyOf(userIds));
            logger.info("Successfully retrieved {} users.", users.size());
            return users;
        } catch (Exception e) {
            logger.error("Error fetching users for userIds: {}. Error: {}", userIds, e.getMessage());
            // 필요 시 사용자 정의 예외로 변환하여 던질 수 있습니다.
            throw e;
        }
    }
}
