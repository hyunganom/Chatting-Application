package chat.userserver.service;

import chat.userserver.event.UserEvent;
import chat.userserver.model.User;
import chat.userserver.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private KafkaTemplate<String, UserEvent> kafkaTemplate;

    private static final String USER_TOPIC = "user-events";

    @Cacheable(value = "user::users", key = "#userId")
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public User createUser(String username, String password) {
        logger.info("Attempting to create user: {}", username);

        // 사용자 이름 중복 확인
        if (userRepository.findByUsername(username).isPresent()) {
            logger.warn("Username already exists: {}", username);
            throw new IllegalArgumentException("Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        User savedUser = userRepository.save(user);

        UserEvent event = new UserEvent("CREATE", savedUser);
        kafkaTemplate.send(USER_TOPIC, event);

        logger.info("User created successfully: {}", savedUser.getUsername());
        return savedUser;
    }

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

    public void logout(String sessionId) {
        logger.info("Attempting logout for sessionId: {}", sessionId);
        sessionService.invalidateSession(sessionId);
        logger.info("Logout successful for sessionId: {}", sessionId);
    }

    @Cacheable(value = "user::users", key = "#ids")
    public List<User> findUsersByIds(List<Long> ids) {
        return userRepository.findByIdIn(ids);
    }

}
