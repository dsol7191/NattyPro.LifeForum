package nattypro.life.forum;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ThreadFollowRepository extends JpaRepository<ThreadFollow, Long> {
    Optional<ThreadFollow> findByUserAndPost(User user, Post post);
    List<ThreadFollow> findByUser(User user);
    boolean existsByUserAndPost(User user, Post post);
}