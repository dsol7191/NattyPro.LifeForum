package nattypro.life.forum;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Comment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String content;
    private String author;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "post_id")
    private Long postId;
    
    // Constructors
    public Comment() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Comment(String content, String author, Long postId) {
        this.content = content;
        this.author = author;
        this.postId = postId;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
}