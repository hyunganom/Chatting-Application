package chat.userserver.controller;

import chat.userserver.model.User;
import chat.userserver.service.SessionService;
import chat.userserver.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 사용자 관련 요청을 처리하는 컨트롤러 클래스.
 */
@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:3001") // 필요 시 외부 설정 파일로 이동 가능
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final SessionService sessionService;

    /**
     * 생성자 주입을 통해 의존성을 주입.
     *
     * @param userService    사용자 서비스
     * @param sessionService 세션 서비스
     */
    public UserController(UserService userService, SessionService sessionService) {
        this.userService = userService;
        this.sessionService = sessionService;
    }

    /**
     * 사용자 등록.
     *
     * @param username 사용자 이름
     * @param password 사용자 비밀번호
     * @return 등록 성공 메시지
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestParam String username, @RequestParam String password) {
        logger.info("사용자 등록 요청: {}", username);
        User user = userService.createUser(username, password);
        return ResponseEntity.ok("사용자 등록 성공: " + user.getUsername());
    }

    /**
     * 사용자 로그인.
     *
     * @param username 사용자 이름
     * @param password 사용자 비밀번호
     * @return 생성된 세션 ID 또는 로그인 실패 메시지
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username, @RequestParam String password) {
        logger.info("로그인 시도: {}", username);
        String sessionId = userService.login(username, password);
        if (sessionId != null) {
            return ResponseEntity.ok(sessionId);
        } else {
            logger.warn("로그인 실패: {}", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 사용자 이름 또는 비밀번호");
        }
    }

    /**
     * 사용자 로그아웃.
     *
     * @param sessionId 세션 ID
     * @return 로그아웃 성공 메시지
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam String sessionId) {
        logger.info("로그아웃 요청: 세션ID={}", sessionId);
        userService.logout(sessionId);
        return ResponseEntity.ok("로그아웃 성공");
    }

    /**
     * 여러 유저 ID로 유저 정보 조회.
     *
     * @param ids 유저 ID 리스트
     * @return 유저 정보 리스트
     */
    @GetMapping("/byIds")
    public ResponseEntity<List<User>> getUsersByIds(@RequestParam List<Long> ids) {
        logger.info("여러 사용자 조회 요청: IDs={}", ids);
        List<User> users = userService.findUsersByIds(ids);
        return ResponseEntity.ok(users);
    }

    /**
     * 세션 ID로부터 유저 ID 조회.
     *
     * @param sessionId 세션 ID
     * @return 유저 ID 또는 404 상태
     */
    @GetMapping("/bySession")
    public ResponseEntity<Long> getIdBySession(@RequestParam String sessionId) {
        logger.info("세션 ID로 사용자 ID 조회 요청: 세션ID={}", sessionId);
        Long userId = sessionService.getUserIdBySession(sessionId);
        if (userId != null) {
            logger.info("사용자 ID 조회 성공: 세션ID={}, 사용자ID={}", sessionId, userId);
            return ResponseEntity.ok(userId);
        } else {
            logger.warn("세션 ID로 사용자 ID 조회 실패 또는 세션 만료: 세션ID={}", sessionId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
