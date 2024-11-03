package chat.messageserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class ChatappChatServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatappChatServerApplication.class, args);
    }

}
