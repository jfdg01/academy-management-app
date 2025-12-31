package app.servicios;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Service for caching encoded passwords to avoid re-encoding the same passwords.
 * This improves performance by storing already-encoded passwords in memory.
 */
@Service
public class ServicioCachePassword {
    
    private final Map<String, String> passwordCache = new ConcurrentHashMap<>();
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public ServicioCachePassword(@Lazy PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Encodes a password, using cached result if available.
     * 
     * @param rawPassword the raw password to encode
     * @return the encoded password
     */
    public String encodePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            return null;
        }
        
        // Check if we already have this password encoded
        String cachedEncodedPassword = passwordCache.get(rawPassword);
        if (cachedEncodedPassword != null) {
            return cachedEncodedPassword;
        }
        
        // Encode the password and cache it
        String encodedPassword = passwordEncoder.encode(rawPassword);
        passwordCache.put(rawPassword, encodedPassword);
        
        return encodedPassword;
    }
    
    /**
     * Clears the password cache.
     * Useful for testing or when memory usage becomes a concern.
     */
    public void clearCache() {
        passwordCache.clear();
    }
    
    /**
     * Gets the current size of the password cache.
     * 
     * @return the number of cached passwords
     */
    public int getCacheSize() {
        return passwordCache.size();
    }
    
    /**
     * Checks if a password is already cached.
     * 
     * @param rawPassword the raw password to check
     * @return true if the password is cached, false otherwise
     */
    public boolean isCached(String rawPassword) {
        return rawPassword != null && passwordCache.containsKey(rawPassword);
    }
} 