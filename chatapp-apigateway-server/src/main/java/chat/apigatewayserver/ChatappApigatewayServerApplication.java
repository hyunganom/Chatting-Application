package chat.apigatewayserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class ChatappApigatewayServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatappApigatewayServerApplication.class, args);
    }

}
