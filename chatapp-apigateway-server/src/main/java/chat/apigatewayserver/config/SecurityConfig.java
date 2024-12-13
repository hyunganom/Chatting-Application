package chat.apigatewayserver.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                // CSRF 보호 비활성화
                .csrf().disable()

                // 모든 요청을 허용 (Gateway 필터에서 인증 처리)
                .authorizeExchange()
                .anyExchange().permitAll()
                .and()

                // HTTP Basic 인증 비활성화
                .httpBasic().disable()
                .formLogin().disable();

        return http.build();
    }
}
