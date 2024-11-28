package chat.userserver.repository;

import chat.userserver.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자(User) 엔티티를 위한 리포지토리 인터페이스.
 * Spring Data JPA를 확장하여 기본적인 CRUD 작업을 지원함.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 사용자 이름(username)을 기준으로 사용자를 조회.
     *
     * @param username 조회할 사용자의 이름
     * @return 주어진 이름과 일치하는 사용자를 Optional로 반환
     */
    Optional<User> findByUsername(String username);

    /**
     * 주어진 사용자 ID 목록에 해당하는 사용자들을 조회.
     *
     * @param ids 조회할 사용자들의 ID 리스트
     * @return ID 리스트에 포함된 모든 사용자의 리스트를 반환
     */
    List<User> findByIdIn(List<Long> ids);
}
