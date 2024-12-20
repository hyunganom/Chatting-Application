package chat.websocketserver.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private AuthenticationHandshakeInterceptor authenticationHandshakeInterceptor; // 인터셉터 주입

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");  // /topic으로 시작하는 메시지를 브로커가 처리
        config.setApplicationDestinationPrefixes("/app");  // 클라이언트에서 보낸 메시지의 prefix
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .addInterceptors(authenticationHandshakeInterceptor) // 인터셉터 추가
                .setAllowedOriginPatterns(
                        "http://localhost:8084",    // 클라이언트 출처 추가
                        "http://localhost:3001",    // 클라이언트 출처 추가
                        "http://localhost:8000",
                        "http://websocket-service:8084"
                )
                .withSockJS();
    }
}
