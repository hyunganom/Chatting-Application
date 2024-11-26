package chat.websocketserver.client;

import chat.websocketserver.model.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "user-service", url = "http://user-service:8081")
public interface UserServiceClient {

    @GetMapping("/users/byIds")
    List<User> getUsersByIds(@RequestParam List<Long> ids);
}
