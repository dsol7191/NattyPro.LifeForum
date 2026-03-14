package nattypro.life.forum;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    
    // Find posts by category
    List<Post> findByCategory(String category);
    
    // Count posts by category
    long countByCategory(String category);
}