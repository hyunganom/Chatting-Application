package chat.websocketserver.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SessionService {

    private static final String USER_SERVICE_URL = "http://user-service:8081/users/bySession?sessionId={sessionId}";

    public Long getUserIdBySession(String sessionId) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            Long userId = restTemplate.getForObject(
                    USER_SERVICE_URL, Long.class, sessionId);
            System.out.println("SessionService - Retrieved userId: " + userId + " for sessionId: " + sessionId);
            return userId;
        } catch (Exception e) {
            System.out.println("SessionService - Error retrieving userId for sessionId: " + sessionId);
            e.printStackTrace();
            return null;
        }
    }
}
