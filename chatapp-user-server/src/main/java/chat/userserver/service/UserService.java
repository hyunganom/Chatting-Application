package chat.userserver.service;

import chat.userserver.Util.JwtTokenProvider;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;
import java.util.List;
import chat.userserver.model.User;
import chat.userserver.repository.UserRepository;
import chat.userserver.event.UserEvent;
import chat.userserver.exception.UserAlreadyExistsException;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final KafkaTemplate<String, UserEvent> kafkaTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    private static final String USER_TOPIC = "user-events";

    @Autowired
    public UserService(UserRepository userRepository, KafkaTemplate<String, UserEvent> kafkaTemplate, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Cacheable(value = "user::users", key = "#userId")
    public User getUserById(Long userId) {
        logger.debug("Fetching user with ID: {}", userId);
        return userRepository.findById(userId).orElse(null);
    }

    @Transactional
    public User createUser(String username, String password) {
        logger.info("Attempting to create user: {}", username);

        if (userRepository.findByUsername(username).isPresent()) {
            logger.warn("Username already exists: {}", username);
            throw new UserAlreadyExistsException("Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // 실제 애플리케이션에서는 비밀번호 해싱을 고려해야 함

        User savedUser = userRepository.save(user);

        UserEvent event = new UserEvent(UserEvent.ACTION_CREATE, savedUser);
        kafkaTemplate.send(USER_TOPIC, event);

        logger.info("User created successfully: {}", savedUser.getUsername());
        return savedUser;
    }

    public String login(String username, String password) {
        logger.info("Attempting login for user: {}", username);
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null && user.getPassword().equals(password)) {
            // 두 개의 매개변수를 전달하도록 수정
            String token = jwtTokenProvider.generateToken(user.getUsername(), user.getId());
            logger.info("Login successful for user: {}, token: {}", username, token);
            // Redis에 토큰 저장
            jwtTokenProvider.storeToken(token, user.getId());
            return token;
        } else {
            logger.warn("Login failed for user: {}", username);
            return null;
        }
    }

    public void logout(String token) {
        logger.info("Attempting logout for token: {}", token);
        jwtTokenProvider.invalidateToken(token);
        logger.info("Logout successful for token: {}", token);
    }

    @Cacheable(value = "user::users", key = "#ids")
    public List<User> findUsersByIds(List<Long> ids) {
        logger.debug("Fetching users with IDs: {}", ids);
        return userRepository.findByIdIn(ids);
    }

    /**
     * 카카오 ID로 사용자 조회
     *
     * @param kakaoId 카카오 고유 사용자 ID
     * @return 사용자 또는 null
     */
    @Cacheable(value = "user::users", key = "'kakao_' + #kakaoId", unless = "#result == null")
    public User findUserByKakaoId(String kakaoId) {
        logger.debug("카카오 ID로 사용자 조회: {}", kakaoId);
        return userRepository.findByKakaoId(kakaoId).orElse(null);
    }

    /**
     * 카카오를 통해 새로운 사용자 등록
     *
     * @param kakaoId  카카오 고유 사용자 ID
     * @param username 사용자명 (카카오에서 받은 이메일 또는 닉네임)
     * @return 등록된 사용자
     */
    @Transactional
    public User registerKakaoUser(String kakaoId, String username) {
        logger.info("새 카카오 사용자 등록: {}", username);

        if (userRepository.findByKakaoId(kakaoId).isPresent()) {
            logger.warn("이미 존재하는 카카오 ID: {}", kakaoId);
            throw new UserAlreadyExistsException("카카오 사용자가 이미 존재합니다.");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(""); // 소셜 로그인이므로 비밀번호는 사용하지 않음
        user.setKakaoId(kakaoId);

        User savedUser = userRepository.save(user);

        UserEvent event = new UserEvent(UserEvent.ACTION_CREATE, savedUser);
        kafkaTemplate.send(USER_TOPIC, event);

        logger.info("카카오 사용자 등록 성공: {}", savedUser.getUsername());
        return savedUser;
    }
}
