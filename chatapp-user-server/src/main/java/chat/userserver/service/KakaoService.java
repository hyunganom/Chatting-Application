package chat.userserver.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import chat.userserver.model.KakaoProfile;
import chat.userserver.model.KakaoTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class KakaoService {

    private static final Logger logger = LoggerFactory.getLogger(KakaoService.class);

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Value("${kakao.token-url}")
    private String tokenUrl;

    @Value("${kakao.user-info-url}")
    private String userInfoUrl;

    private final RestTemplate restTemplate;

    public KakaoService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * 인증 코드를 액세스 토큰으로 교환
     *
     * @param code 카카오로부터 받은 인증 코드
     * @return 액세스 토큰
     */
    public String getAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<KakaoTokenResponse> response = restTemplate.postForEntity(tokenUrl, request, KakaoTokenResponse.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            logger.info("액세스 토큰을 성공적으로 가져왔습니다.");
            return response.getBody().getAccess_token();
        } else {
            logger.error("카카오로부터 액세스 토큰을 가져오는데 실패했습니다.");
            throw new RuntimeException("액세스 토큰 가져오기 실패");
        }
    }

    /**
     * 액세스 토큰을 사용하여 카카오에서 사용자 프로필 조회
     *
     * @param accessToken 카카오로부터 받은 액세스 토큰
     * @return 카카오 사용자 프로필 정보
     */
    public KakaoProfile getKakaoProfile(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.add("Accept", "application/json");

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<KakaoProfile> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, KakaoProfile.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            logger.info("카카오 프로필을 성공적으로 가져왔습니다.");
            return response.getBody();
        } else {
            logger.error("카카오 프로필을 가져오는데 실패했습니다.");
            throw new RuntimeException("카카오 프로필 가져오기 실패");
        }
    }
}
