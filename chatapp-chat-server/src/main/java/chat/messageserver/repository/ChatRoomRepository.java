package chat.messageserver.repository;

import chat.messageserver.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 채팅방 관련 데이터 접근을 담당하는 리포지토리 인터페이스.
 * Spring Data JPA를 사용하여 CRUD 및 커스텀 쿼리 메서드를 제공함.
 */
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    /**
     * 특정 이름을 가진 채팅방이 존재하는지 확인함.
     *
     * @param roomName 채팅방 이름
     * @return 존재 여부 (true: 존재함, false: 존재하지 않음)
     */
    boolean existsByRoomName(String roomName);

    /**
     * 특정 이름을 가진 채팅방을 조회함.
     *
     * @param roomName 채팅방 이름
     * @return 해당 이름을 가진 채팅방을 Optional로 반환함
     */
    Optional<ChatRoom> findByRoomName(String roomName);
}
