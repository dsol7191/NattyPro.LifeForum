package nattypro.life.forum;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {
    
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ThreadFollowRepository followRepo;
    @Autowired
    private PostVoteRepository postVoteRepo;
    
 // Define all categories in one place
    private static final List<String> CATEGORIES = Arrays.asList(
        "Training",
        "Nutrition",
        "Recovery", 
        "Mindset",
        "Prep Files",
        "Coaches Corner",
        "Self Promotion",
        "Random Side Quests"
        
    );
    
    @GetMapping("/")
    public String home(@RequestParam(required = false) String category, Model model, Authentication authentication) {
        	//Filter Post by category
    	List<Post> posts;
    	// Add current user info for profile links
    	if (category != null && !category.isEmpty()) {
    	    posts = postRepository.findByCategory(category);
    	    model.addAttribute("selectedCategory", category);
    	} else {
    	    posts = postRepository.findAll().stream()
    	        .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
    	        .limit(5)
    	        .collect(Collectors.toList());
    	    model.addAttribute("selectedCategory", "All");
    	    
    	 // Build a map of username -> verifiedNattyPro for all post authors
    	    Map<String, Object> proStatusMap = posts.stream()
    	    	    .collect(Collectors.toMap(
    	    	        Post::getAuthor,
    	    	        p -> {
    	    	            User u = userRepository.findByUsername(p.getAuthor()).orElse(null);
    	    	            return u != null && u.getVerifiedNattyPro() != null && u.getVerifiedNattyPro();
    	    	        },
    	    	        (a, b) -> a
    	    	    ));
    	    	model.addAttribute("proStatusMap", proStatusMap);
    	    	// PRO status for post author
    	    	
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
    public String viewPost(@PathVariable Long id, Model model, Authentication authentication) {
        Post post = postRepository.findById(id).orElse(null);

        if (post == null) {
            return "redirect:/";
        }

        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(id);

        model.addAttribute("post", post);
        model.addAttribute("comments", comments);
        model.addAttribute("newComment", new Comment());
        User postAuthor = userRepository.findByUsername(post.getAuthor()).orElse(null);
    	boolean postAuthorPro = postAuthor != null && Boolean.TRUE.equals(postAuthor.getVerifiedNattyPro());
    	model.addAttribute("postAuthorPro", postAuthorPro);

    	// PRO status map for comment authors
    	Map<String, Object> commentProStatus = comments.stream()
    	    .collect(Collectors.toMap(
    	        Comment::getAuthor,
    	        c -> {
    	            User u = userRepository.findByUsername(c.getAuthor()).orElse(null);
    	            return u != null && Boolean.TRUE.equals(u.getVerifiedNattyPro());
    	        },
    	        (a, b) -> a
    	    ));
    	model.addAttribute("commentProStatus", commentProStatus);
        
        // Current user for profile links and PRO badge
        if (authentication != null) {
            User currentUser = userRepository.findByUsername(authentication.getName()).orElse(null);
            model.addAttribute("currentUser", currentUser);
            
        }
        if (authentication != null) {
            User currentUser = userRepository.findByUsername(authentication.getName()).orElse(null);
            model.addAttribute("currentUser", currentUser);
            
            // Add these:
            if (currentUser != null) {
                boolean isFollowing = followRepo.existsByUserAndPost(currentUser, post);
                boolean hasVoted = postVoteRepo.findByUserAndPost(currentUser, post).isPresent();
                model.addAttribute("isFollowing", isFollowing);
                model.addAttribute("hasVoted", hasVoted);
            }
        } else {
            model.addAttribute("isFollowing", false);
            model.addAttribute("hasVoted", false);
        }
        
        return "post";
    }
    
    @GetMapping("/search")
    public String search(@RequestParam String query, Model model, Authentication authentication) {
        
        List<Post> results = postRepository.findAll().stream()
            .filter(p -> 
                p.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                p.getContent().toLowerCase().contains(query.toLowerCase()) ||
                p.getAuthor().toLowerCase().contains(query.toLowerCase())
            )
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .collect(Collectors.toList());

        // PRO status map for results
        Map<String, Object> proStatusMap = results.stream()
            .collect(Collectors.toMap(
                Post::getAuthor,
                p -> {
                    User u = userRepository.findByUsername(p.getAuthor()).orElse(null);
                    return u != null && Boolean.TRUE.equals(u.getVerifiedNattyPro());
                },
                (a, b) -> a
            ));

        model.addAttribute("results", results);
        model.addAttribute("query", query);
        model.addAttribute("proStatusMap", proStatusMap);
        model.addAttribute("resultCount", results.size());

        // User stats for header
        if (authentication != null) {
            User currentUser = userRepository.findByUsername(authentication.getName()).orElse(null);
            model.addAttribute("currentUser", currentUser);
            long userPostCount = postRepository.findAll().stream()
                .filter(p -> p.getAuthor().equals(authentication.getName()))
                .count();
            String userRank;
            String rankEmoji;
            if (userPostCount == 0) { userRank = "Lurker"; rankEmoji = "👀"; }
            else if (userPostCount <= 2) { userRank = "Newb"; rankEmoji = "🌱"; }
            else if (userPostCount <= 7) { userRank = "Member"; rankEmoji = "💪"; }
            else if (userPostCount <= 14) { userRank = "Regular"; rankEmoji = "⭐"; }
            else if (userPostCount <= 29) { userRank = "Veteran"; rankEmoji = "🔥"; }
            else { userRank = "Legend"; rankEmoji = "👑"; }
            model.addAttribute("userPostCount", userPostCount);
            model.addAttribute("userRank", userRank);
            model.addAttribute("rankEmoji", rankEmoji);
        }

        return "search-results";
    }

    @PostMapping("/post/{id}/comment")
    public String addComment(@PathVariable Long id, @RequestParam String content, Authentication authentication) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setPostId(id);
        comment.setAuthor(authentication.getName());
        comment.setCreatedAt(LocalDateTime.now());  // ← Add this line
        commentRepository.save(comment);
        return "redirect:/post/" + id;
    }
}