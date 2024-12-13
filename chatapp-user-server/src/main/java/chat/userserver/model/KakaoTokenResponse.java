package chat.userserver.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class KakaoTokenResponse {
    private String access_token;
    private String token_type;
    private Long expires_in;
    private String refresh_token;
    private String refresh_token_expires_in;
    private String scope;
}
