package chat.websocketserver.event;

import chat.websocketserver.model.Message;

import java.io.Serializable;

public class MessageEvent implements Serializable {
    private String action; // SEND, DELETE
    private Message message;

    public MessageEvent() {}

    public MessageEvent(String action, Message message) {
        this.action = action;
        this.message = message;
    }

    // Getters and Setters
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}