package nattypro.life.forum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Arrays;

@Controller
public class HomeController {
    
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;
    
    
 // Define all categories in one place
    private static final List<String> CATEGORIES = Arrays.asList(
        "Training",
        "Nutrtion",
        "Recovery", 
        "Mindset",
        "Prep Files",
        "Coaches Corner",
        "Self-Promotion",
        "Random Side Quests"
        
    );
    
    @GetMapping("/")
    public String home(@RequestParam(required = false) String category, Model model, Authentication authentication) {
        	//Filter Post by category
    	List<Post> posts;
    	// Add current user info for profile links
    	 if (category != null && !category.isEmpty()) {
    	        posts = postRepository.findByCategory(category);  // Assigned here
    	        model.addAttribute("selectedCategory", category);
    	}
    	else {
            posts = postRepository.findAll();
            model.addAttribute("selectedCategory", "All");
        }
        
        
        model.addAttribute("posts", posts);
        model.addAttribute("newPost", new Post());
        
        // Add category counts for sidebar
     // Add category counts for sidebar
        model.addAttribute("trainingCount", postRepository.countByCategory("Training"));
        model.addAttribute("nutritionCount", postRepository.countByCategory("Nutrition"));
        model.addAttribute("recoveryCount", postRepository.countByCategory("Recovery"));
        model.addAttribute("mindsetCount", postRepository.countByCategory("Mindset"));
        model.addAttribute("prepFilesCount", postRepository.countByCategory("Prep Files"));
        model.addAttribute("coachesCornerCount", postRepository.countByCategory("Coaches Corner"));
        model.addAttribute("selfPromotionCount", postRepository.countByCategory("Self Promotion"));
        model.addAttribute("randomSideQuestsCount", postRepository.countByCategory("Random Side Quests"));
        model.addAttribute("totalCount", postRepository.count());
        model.addAttribute("categories", CATEGORIES);
        
     // User stats and rank for header
        if (authentication != null) {
            User currentUser = userRepository.findByUsername(authentication.getName()).orElse(null);
            model.addAttribute("currentUser", currentUser);
            
            // Count user's posts
            long userPostCount = postRepository.findAll().stream()
                .filter(p -> p.getAuthor().equals(authentication.getName()))
                .count();
            
            // Determine rank based on post count
            String userRank;
            String rankEmoji;
            if (userPostCount == 0) {
                userRank = "Lurker";
                rankEmoji = "👀";
            } else if (userPostCount <= 2) {
                userRank = "Newb";
                rankEmoji = "🌱";
            } else if (userPostCount <= 7) {
                userRank = "Member";
                rankEmoji = "💪";
            } else if (userPostCount <= 14) {
                userRank = "Regular";
                rankEmoji = "⭐";
            } else if (userPostCount <= 29) {
                userRank = "Veteran";
                rankEmoji = "🔥";
            } else {
                userRank = "Legend";
                rankEmoji = "👑";
            }
            
            model.addAttribute("userPostCount", userPostCount);
            model.addAttribute("userRank", userRank);
            model.addAttribute("rankEmoji", rankEmoji);
        }
        
        return "home";
    }
    
    @PostMapping("/create")
    public String createPost(Post post, Authentication authentication) {
        // Automatically set author to logged-in username
        post.setAuthor(authentication.getName());
        postRepository.save(post);
        return "redirect:/";
        
    }
    @PostMapping("/delete/{id}")
    public String deletePost(@PathVariable Long id, Authentication authentication) {
        Post post = postRepository.findById(id).orElse(null);
        
        if (post != null) {
            String currentUser = authentication.getName();
            boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            // Allow deletion if user is the author OR an admin
            if (post.getAuthor().equals(currentUser) || isAdmin) {
                postRepository.delete(post);
            }
        }
        
        return "redirect:/";
    }

    @GetMapping("/post/{id}")
    public String viewPost(@PathVariable Long id, Model model) {
        Post post = postRepository.findById(id).orElse(null);
        
        if (post == null) {
            return "redirect:/";
        }
        
        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(id);
        
        model.addAttribute("post", post);
        model.addAttribute("comments", comments);
        model.addAttribute("newComment", new Comment());
        
        return "post";
    }

    @PostMapping("/post/{id}/comment")
    public String addComment(@PathVariable Long id, @RequestParam String content, Authentication authentication) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setPostId(id);
        comment.setAuthor(authentication.getName());
        commentRepository.save(comment);
        return "redirect:/post/" + id;
    
    }
}