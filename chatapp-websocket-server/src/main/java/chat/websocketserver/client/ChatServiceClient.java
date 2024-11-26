package chat.websocketserver.client;

import chat.websocketserver.model.ChatRoom;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "chat-service", url = "http://chat-service:8082")
public interface ChatServiceClient {

    @GetMapping("/chatrooms/{roomId}")
    ChatRoom getChatRoomById(@PathVariable("roomId") Long roomId);
}
