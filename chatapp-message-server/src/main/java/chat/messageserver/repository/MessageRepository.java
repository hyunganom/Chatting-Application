package chat.messageserver.repository;

import chat.messageserver.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {
    Page<Message> findByRoomId(Long roomId, Pageable pageable);
    List<Message> findByRoomId(Long roomId);
}

