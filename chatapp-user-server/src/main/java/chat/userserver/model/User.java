package chat.userserver.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 사용자 정보를 나타내는 엔티티 클래스.
 * 데이터베이스의 'users' 테이블과 매핑됨.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User implements Serializable {

    /**
     * 사용자 고유 식별자.
     * 자동으로 생성되며, 기본 키로 설정됨.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사용자의 고유한 이름.
     * 중복을 허용하지 않으며, null 값이 될 수 없음.
     */
    @Column(unique = true, nullable = false)
    private String username;

    /**
     * 사용자의 비밀번호.
     * null 값이 될 수 없으며, 보안을 위해 적절히 암호화하여 저장해야 함.
     */
    @Column(nullable = false)
    private String password;

    /**
     * 카카오 소셜 로그인 필드
     */
    @Column(unique = true)
    private String kakaoId; // 카카오 소셜 로그인을 위한 필드 추가

    /**
     * 객체의 문자열 표현을 커스터마이징.
     * 비밀번호는 출력하지 않도록 설정.
     * @return 사용자 정보를 포함한 문자열
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                '}';
    }
}
