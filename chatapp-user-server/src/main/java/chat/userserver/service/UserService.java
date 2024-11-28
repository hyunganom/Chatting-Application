package chat.userserver.service;

import chat.userserver.event.UserEvent;
import chat.userserver.exception.UserAlreadyExistsException;
import chat.userserver.model.User;
import chat.userserver.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스 클래스.
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final SessionService sessionService;
    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    private static final String USER_TOPIC = "user-events";

    /**
     * 생성자 주입을 통해 의존성을 주입.
     *
     * @param userRepository  사용자 리포지토리
     * @param sessionService  세션 서비스
     * @param kafkaTemplate   카프카 템플릿
     */
    public UserService(UserRepository userRepository, SessionService sessionService, KafkaTemplate<String, UserEvent> kafkaTemplate) {
        this.userRepository = userRepository;
        this.sessionService = sessionService;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * 사용자 ID로 사용자를 조회.
     * 캐시에 저장되어 있는 경우 캐시에서 반환.
     *
     * @param userId 조회할 사용자 ID
     * @return 사용자 객체 또는 null
     */
    @Cacheable(value = "user::users", key = "#userId")
    public User getUserById(Long userId) {
        logger.debug("Fetching user with ID: {}", userId);
        return userRepository.findById(userId).orElse(null);
    }

    /**
     * 새로운 사용자를 생성하고 이벤트를 카프카에 전송.
     *
     * @param username 생성할 사용자 이름
     * @param password 생성할 사용자 비밀번호
     * @return 생성된 사용자 객체
     * @throws UserAlreadyExistsException 사용자 이름이 이미 존재할 경우 예외 발생
     */
    @Transactional
    public User createUser(String username, String password) {
        logger.info("Attempting to create user: {}", username);

        // 사용자 이름 중복 확인
        if (userRepository.findByUsername(username).isPresent()) {
            logger.warn("Username already exists: {}", username);
            throw new UserAlreadyExistsException("Username already exists");
        }

        // 새로운 사용자 생성
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        // 사용자 저장
        User savedUser = userRepository.save(user);

        // 사용자 생성 이벤트 생성 및 전송
        UserEvent event = new UserEvent(UserEvent.ACTION_CREATE, savedUser);
        kafkaTemplate.send(USER_TOPIC, event);

        logger.info("User created successfully: {}", savedUser.getUsername());
        return savedUser;
    }

    /**
     * 사용자가 로그인할 때 세션을 생성.
     *
     * @param username 로그인할 사용자 이름
     * @param password 로그인할 사용자 비밀번호
     * @return 생성된 세션 ID 또는 null
     */
    public String login(String username, String password) {
        logger.info("Attempting login for user: {}", username);
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null && user.getPassword().equals(password)) {
            String sessionId = sessionService.createSession(user.getId(), username);
            logger.info("Login successful for user: {}, sessionId: {}", username, sessionId);
            return sessionId;
        } else {
            logger.warn("Login failed for user: {}", username);
            return null;
        }
    }

    /**
     * 사용자가 로그아웃할 때 세션을 무효화.
     *
     * @param sessionId 로그아웃할 세션 ID
     */
    public void logout(String sessionId) {
        logger.info("Attempting logout for sessionId: {}", sessionId);
        sessionService.invalidateSession(sessionId);
        logger.info("Logout successful for sessionId: {}", sessionId);
    }

    /**
     * 여러 사용자 ID로 사용자를 조회.
     * 캐시에 저장되어 있는 경우 캐시에서 반환.
     *
     * @param ids 조회할 사용자 ID 목록
     * @return 사용자 리스트
     */
    @Cacheable(value = "user::users", key = "#ids")
    public List<User> findUsersByIds(List<Long> ids) {
        logger.debug("Fetching users with IDs: {}", ids);
        return userRepository.findByIdIn(ids);
    }
}
