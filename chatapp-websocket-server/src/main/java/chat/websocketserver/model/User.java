package chat.websocketserver.model;

import java.io.Serializable;

public class User implements Serializable {
    private Long id;
    private String username;
    // 추가 필드...

    public User() {}

    public User(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
