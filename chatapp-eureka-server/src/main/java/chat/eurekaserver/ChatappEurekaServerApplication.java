package chat.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class ChatappEurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatappEurekaServerApplication.class, args);
    }

}
