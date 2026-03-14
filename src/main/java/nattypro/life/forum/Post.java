package nattypro.life.forum;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Post {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private String content;
    private String author;
    private String category;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Constructors
    public Post() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Post(String title, String content, String author, String category) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.category = category;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}