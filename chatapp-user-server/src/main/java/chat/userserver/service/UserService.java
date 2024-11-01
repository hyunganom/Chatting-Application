package chat.userserver.service;

import chat.userserver.model.User;
import chat.userserver.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionService sessionService;

    public User createUser(String username, String password) {
        logger.info("Attempting to create user: {}", username);
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // 실제로는 비밀번호 암호화 필요
        User savedUser = userRepository.save(user);
        logger.info("User created successfully: {}", savedUser.getUsername());
        return savedUser;
    }

    public String login(String username, String password) {
        logger.info("Attempting login for user: {}", username);
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null && user.getPassword().equals(password)) {
            String sessionId = sessionService.createSession(username);
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
}
