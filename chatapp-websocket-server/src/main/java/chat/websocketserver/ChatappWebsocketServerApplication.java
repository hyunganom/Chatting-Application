package chat.websocketserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class ChatappWebsocketServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatappWebsocketServerApplication.class, args);
    }

}
