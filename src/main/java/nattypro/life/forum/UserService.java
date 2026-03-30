package nattypro.life.forum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    
    public void validateNewUser(String username, String email) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
    }

    public void setAcceptedRules(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        user.setAcceptedRules(true);
        userRepository.save(user);
    }
    
    public User registerUser(String username, String password, String email) {
        // Encrypt the password before saving
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(username, encodedPassword, email);
        return userRepository.save(user);
    }
    
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
}