package chat.userserver.controller;

import chat.userserver.model.User;
import chat.userserver.service.SessionService;
import chat.userserver.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:3001")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private SessionService sessionService;

    /**
     * 사용자 등록
     *
     * @param username 사용자 이름
     * @param password 사용자 비밀번호
     * @return 등록 성공 메시지
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestParam String username, @RequestParam String password) {
        logger.info("Received request to register user: {}", username);
        try {
            User user = userService.createUser(username, password);
            return ResponseEntity.ok("User registered: " + user.getUsername());
        } catch (IllegalArgumentException e) {
            logger.error("Registration failed for user: {}", username, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * 사용자 로그인
     *
     * @param username 사용자 이름
     * @param password 사용자 비밀번호
     * @return 생성된 세션 ID 또는 로그인 실패 메시지
     */
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

    /**
     * 사용자 로그아웃
     *
     * @param sessionId 세션 ID
     * @return 로그아웃 성공 메시지
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam String sessionId) {
        logger.info("Received logout request for sessionId: {}", sessionId);
        userService.logout(sessionId);
        return ResponseEntity.ok("Logged out successfully");
    }

    /**
     * 여러 유저 ID로 유저 정보 조회
     *
     * @param ids 유저 ID 리스트
     * @return 유저 정보 리스트
     */
    @GetMapping("/byIds")
    public ResponseEntity<List<User>> getUsersByIds(@RequestParam List<Long> ids) {
        List<User> users = userService.findUsersByIds(ids);
        return ResponseEntity.ok(users);
    }

    /**
     * 세션 ID로부터 유저 ID 조회
     *
     * @param sessionId 세션 ID
     * @return 유저 ID 또는 null
     */
    @GetMapping("/bySession")
    public ResponseEntity<Long> getIdBySession(@RequestParam String sessionId) {
        Long userId = sessionService.getUserIdBySession(sessionId);
        System.out.println("Received request for userId by sessionId: " + sessionId);
        if (userId != null) {
            System.out.println("Returning userId: " + userId);
            return ResponseEntity.ok(userId);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
