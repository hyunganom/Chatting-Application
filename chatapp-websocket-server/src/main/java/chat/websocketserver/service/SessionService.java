package chat.websocketserver.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class SessionService {

    private static final Logger logger = LoggerFactory.getLogger(SessionService.class);
    private static final String USER_SERVICE_URL = "http://user-service:8081/users/bySession?sessionId={sessionId}";

    private final RestTemplate restTemplate;

    /**
     * 생성자 주입을 통해 RestTemplate을 주입받음.
     *
     * @param restTemplate RestTemplate 빈
     */
    public SessionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 세션 ID를 기반으로 사용자 ID를 조회함.
     *
     * @param sessionId 세션 ID
     * @return 사용자 ID 또는 null
     */
    public Long getUserIdBySession(String sessionId) {
        try {
            Long userId = restTemplate.getForObject(USER_SERVICE_URL, Long.class, sessionId);
            logger.info("SessionService - Retrieved userId: {} for sessionId: {}", userId, sessionId);
            return userId;
        } catch (RestClientException e) {
            logger.error("SessionService - Error retrieving userId for sessionId: {}", sessionId, e);
            return null;
        }
    }
}
