package chat.userserver.controller;

import chat.userserver.model.User;
import chat.userserver.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestParam String username, @RequestParam String password) {
        logger.info("Received request to register user: {}", username);
        User user = userService.createUser(username, password);
        return ResponseEntity.ok("User registered: " + user.getUsername());
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username, @RequestParam String password) {
        logger.info("Received login request for user: {}", username);
        String sessionId = userService.login(username, password);
        if (sessionId != null) {
            return ResponseEntity.ok(sessionId);
        } else {
            logger.warn("Login failed for user: {}", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam String sessionId) {
        logger.info("Received logout request for sessionId: {}", sessionId);
        userService.logout(sessionId);
        return ResponseEntity.ok("Logged out successfully");
    }
}
