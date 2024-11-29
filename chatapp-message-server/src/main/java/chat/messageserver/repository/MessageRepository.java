package chat.messageserver.repository;

import chat.messageserver.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * 메시지 관련 데이터 접근을 담당하는 리포지토리 인터페이스.
 * Spring Data MongoDB를 사용하여 CRUD 및 커스텀 쿼리 메서드를 제공함.
 */
public interface MessageRepository extends MongoRepository<Message, String> {

    /**
     * 특정 채팅방 ID에 속한 메시지를 페이지네이션하여 조회함.
     *
     * @param roomId   채팅방 ID
     * @param pageable 페이지네이션 및 정렬 정보를 담은 Pageable 객체
     * @return 특정 채팅방의 메시지 페이지
     */
    Page<Message> findByRoomId(Long roomId, Pageable pageable);

    /**
     * 특정 채팅방 ID에 속한 모든 메시지를 조회함.
     *
     * @param roomId 채팅방 ID
     * @return 특정 채팅방의 메시지 리스트
     */
    List<Message> findByRoomId(Long roomId);
}
