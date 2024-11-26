package chat.websocketserver.service;

import chat.websocketserver.client.UserServiceClient;
import chat.websocketserver.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private UserServiceClient userServiceClient;

    /**
     * 유저 ID 목록을 받아 유저 정보 리스트 반환
     */
    public List<User> getUsersByIds(Set<Long> userIds) {
        if (userIds.isEmpty()) {
            return List.of();
        }
        return userServiceClient.getUsersByIds(List.copyOf(userIds));
    }
}