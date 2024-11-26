package chat.websocketserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients(basePackages = "chat.websocketserver.client")
public class ChatappWebsocketServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatappWebsocketServerApplication.class, args);
    }

}
