package nattypro.life.forum;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;


@Transactional
void deleteByPost(Post post);

public interface ThreadFollowRepository extends JpaRepository<ThreadFollow, Long> {
    Optional<ThreadFollow> findByUserAndPost(User user, Post post);
    List<ThreadFollow> findByUser(User user);
    boolean existsByUserAndPost(User user, Post post);
    void deleteByPost(Post post);
}


