package chat.userserver.controller;

import chat.userserver.Util.JwtTokenProvider;
import chat.userserver.model.KakaoProfile;
import chat.userserver.model.User;
import chat.userserver.service.KakaoService;
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
@CrossOrigin(origins = "*")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);



    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final KakaoService kakaoService;

    /**
     * 생성자 주입을 통해 의존성을 주입.
     */
    public UserController(UserService userService, JwtTokenProvider jwtTokenProvider, KakaoService kakaoService) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.kakaoService = kakaoService;
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
//    @GetMapping("/bySession")
//    public ResponseEntity<Long> getIdBySession(@RequestParam String sessionId) {
//        logger.info("세션 ID로 사용자 ID 조회 요청: 세션ID={}", sessionId);
//        Long userId = sessionService.getUserIdBySession(sessionId);
//        if (userId != null) {
//            logger.info("사용자 ID 조회 성공: 세션ID={}, 사용자ID={}", sessionId, userId);
//            return ResponseEntity.ok(userId);
//        } else {
//            logger.warn("세션 ID로 사용자 ID 조회 실패 또는 세션 만료: 세션ID={}", sessionId);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        }
//    }
    /**
     * 카카오 소셜 로그인 엔드포인트
     *
     * @param request 카카오로부터 받은 인증 코드가 포함된 요청
     * @return JWT 토큰
     */
    @PostMapping("/kakao-login")
    public ResponseEntity<String> kakaoLogin(@RequestBody KakaoLoginRequest request) {
        String code = request.getCode();
        logger.info("카카오 로그인 시도, 코드: {}", code);
        try {
            String accessToken = kakaoService.getAccessToken(code);
            KakaoProfile kakaoProfile = kakaoService.getKakaoProfile(accessToken);

            // 카카오 프로필에서 필요한 정보 추출
            String kakaoId = String.valueOf(kakaoProfile.getId());
            String username = kakaoProfile.getKakao_account().getEmail(); // 이메일이 없을 경우 다른 식별자로 대체 필요
            if (username == null || username.isEmpty()) {
                username = kakaoProfile.getKakao_account().getProfile().getNickname();
            }

            // 사용자 등록 또는 기존 사용자 조회
            User user = userService.findUserByKakaoId(kakaoId);
            if (user == null) {
                user = userService.registerKakaoUser(kakaoId, username);
            }

            // JWT 토큰 생성
            String token = jwtTokenProvider.generateToken(user.getUsername(), user.getId());
            jwtTokenProvider.storeToken(token, user.getId());

            logger.info("카카오 로그인 성공, 사용자: {}", username);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            logger.error("카카오 로그인 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kakao login failed");
        }
    }

    /**
     * Authorization 헤더에서 Bearer 토큰 추출
     *
     * @param authorizationHeader Authorization 헤더 값
     * @return 토큰 문자열
     */
    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }

    /**
     * 카카오 로그인 요청을 위한 DTO 클래스
     */
    public static class KakaoLoginRequest {
        private String code;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

}
