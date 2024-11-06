package chat.messageserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class ChatappMessageServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatappMessageServerApplication.class, args);
    }

}
