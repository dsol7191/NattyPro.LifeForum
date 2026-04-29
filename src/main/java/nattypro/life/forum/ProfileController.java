package nattypro.life.forum;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ProfileController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private PostVoteRepository postVoteRepo;
    @Autowired
    private ThreadFollowRepository followRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    
    
    // Directory for avatar uploads
    private final String UPLOAD_DIR = "uploads/avatars/";
    
    @GetMapping("/profile/{username}")
    public String viewProfile(@PathVariable String username, Model model, Authentication authentication) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Post> userPosts = postRepository.findAll().stream()
            .filter(p -> p.getAuthor().equals(username))
            .collect(Collectors.toList());
        
        List<Comment> userComments = commentRepository.findAll().stream()
            .filter(c -> c.getAuthor().equals(username))
            .collect(Collectors.toList());
        
        long reputation = postVoteRepo.countByPostIn(userPosts);
        
        List<ThreadFollow> followedThreads = followRepo.findByUser(user);
        
        model.addAttribute("followedThreads", followedThreads);
        model.addAttribute("reputation", reputation);
        model.addAttribute("profileUser", user);
        model.addAttribute("userPosts", userPosts);
        model.addAttribute("usferComments", userComments);
        
        if (authentication != null) {
            model.addAttribute("isOwnProfile", authentication.getName().equals(username));
        } else {
            model.addAttribute("isOwnProfile", false);
        }
        
        return "profile";
    }
    
    @GetMapping("/profile/edit")
    public String editProfilePage(Authentication authentication, Model model) {
        User user = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        return "profile-edit";
    }
    
    @PostMapping("/profile/update")
    public String updateProfile(
        @RequestParam(required = false) String bio,
        @RequestParam(required = false) Integer age,
        @RequestParam(required = false) String instagramHandle,
        @RequestParam(required = false) String twitterHandle,
        @RequestParam(required = false) String youtubeHandle,
        @RequestParam(required = false) String twitchHandle,
        @RequestParam(required = false, defaultValue = "false") Boolean showEmail,
        @RequestParam(required = false, defaultValue = "true") Boolean showAge,
        @RequestParam(required = false) MultipartFile avatarFile,
        Authentication authentication) {
        
    	
    	
        User user = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        
        
        // Validate and set bio
        if (bio != null && bio.length() > 500) {
            bio = bio.substring(0, 500);
            
            
        }
        
        
        user.setBio(bio);
        
        // Validate and set age
        if (age != null && (age < 13 || age > 120)) {
            age = null;
        }
        user.setAge(age);
        
        // Set social handles
        user.setInstagramHandle(instagramHandle);
        user.setTwitterHandle(twitterHandle);
        user.setYoutubeHandle(youtubeHandle);
        user.setTwitchHandle(twitchHandle);
        
        // Set privacy settings
        user.setShowEmail(showEmail);
        user.setShowAge(showAge);
        
        // Handle avatar upload
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                // Validate file type
                String contentType = avatarFile.getContentType();
                if (contentType != null && (contentType.equals("image/jpeg") || 
                    contentType.equals("image/png") || 
                    contentType.equals("image/jpg") ||
                    contentType.equals("image/gif") ||
                    contentType.equals("image/webp"))) {
                    
                    // Create uploads directory if it doesn't exist
                    Path uploadPath = Paths.get(UPLOAD_DIR);
                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }
                    
                    // Generate unique filename
                    String originalFilename = avatarFile.getOriginalFilename();
                    String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                    String newFilename = UUID.randomUUID().toString() + extension;
                    
                    // Save file
                    Path filePath = uploadPath.resolve(newFilename);
                    Files.copy(avatarFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                    
                    // Update user's avatar URL
                    user.setAvatarUrl("/avatars/" + newFilename);
                }
            } catch (IOException e) {
                e.printStackTrace();
                // If upload fails, just continue without updating avatar
            }
        }
        
        userRepository.save(user);
      
        return "redirect:/profile/" + authentication.getName() + "?updated=true";

  
    }
    @PostMapping("/profile/removeAvatar")
    public String removeAvatar(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setAvatarUrl("/images/default-avatar.png");
        userRepository.save(user);
        
        return "redirect:/profile/edit";
    }
}