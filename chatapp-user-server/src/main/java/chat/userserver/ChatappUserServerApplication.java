package chat.userserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class ChatappUserServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatappUserServerApplication.class, args);
    }

}
