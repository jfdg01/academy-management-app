package app.servicios;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioCachePasswordTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    private ServicioCachePassword servicioCachePassword;

    @BeforeEach
    void setUp() {
        servicioCachePassword = new ServicioCachePassword(passwordEncoder);
    }

    @Test
    void testEncodePassword_FirstTime_ShouldEncodeAndCache() {
        // Arrange
        String rawPassword = "testPassword123";
        String expectedEncoded = "$2a$10$sNAGMabwHrFB5jIE9GEVJ.3yFQzQ2PteY5YAkUREkiztqQ1QQniCy";
        when(passwordEncoder.encode(rawPassword)).thenReturn(expectedEncoded);

        // Act
        String result = servicioCachePassword.encodePassword(rawPassword);

        // Assert
        assertEquals(expectedEncoded, result);
        verify(passwordEncoder, times(1)).encode(rawPassword);
        assertTrue(servicioCachePassword.isCached(rawPassword));
        assertEquals(1, servicioCachePassword.getCacheSize());
    }

    @Test
    void testEncodePassword_SecondTime_ShouldReturnCached() {
        // Arrange
        String rawPassword = "testPassword123";
        String expectedEncoded = "$2a$10$sNAGMabwHrFB5jIE9GEVJ.3yFQzQ2PteY5YAkUREkiztqQ1QQniCy";
        when(passwordEncoder.encode(rawPassword)).thenReturn(expectedEncoded);

        // Act - First call
        String firstResult = servicioCachePassword.encodePassword(rawPassword);
        // Act - Second call
        String secondResult = servicioCachePassword.encodePassword(rawPassword);

        // Assert
        assertEquals(expectedEncoded, firstResult);
        assertEquals(expectedEncoded, secondResult);
        // Should only call encode once
        verify(passwordEncoder, times(1)).encode(rawPassword);
        assertEquals(1, servicioCachePassword.getCacheSize());
    }

    @Test
    void testEncodePassword_NullPassword_ShouldReturnNull() {
        // Act
        String result = servicioCachePassword.encodePassword(null);

        // Assert
        assertNull(result);
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void testEncodePassword_EmptyPassword_ShouldReturnNull() {
        // Act
        String result = servicioCachePassword.encodePassword("");

        // Assert
        assertNull(result);
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void testEncodePassword_WhitespacePassword_ShouldReturnNull() {
        // Act
        String result = servicioCachePassword.encodePassword("   ");

        // Assert
        assertNull(result);
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void testClearCache_ShouldRemoveAllCachedPasswords() {
        // Arrange
        String password1 = "password1";
        String password2 = "password2";
        when(passwordEncoder.encode(password1)).thenReturn("$2a$10$encoded1.hash.value.here");
        when(passwordEncoder.encode(password2)).thenReturn("$2a$10$encoded2.hash.value.here");

        // Act
        servicioCachePassword.encodePassword(password1);
        servicioCachePassword.encodePassword(password2);
        assertEquals(2, servicioCachePassword.getCacheSize());

        servicioCachePassword.clearCache();

        // Assert
        assertEquals(0, servicioCachePassword.getCacheSize());
        assertFalse(servicioCachePassword.isCached(password1));
        assertFalse(servicioCachePassword.isCached(password2));
    }

    @Test
    void testGetCacheSize_ShouldReturnCorrectSize() {
        // Arrange
        String password1 = "password1";
        String password2 = "password2";
        when(passwordEncoder.encode(password1)).thenReturn("$2a$10$encoded1.hash.value.here");
        when(passwordEncoder.encode(password2)).thenReturn("$2a$10$encoded2.hash.value.here");

        // Act & Assert
        assertEquals(0, servicioCachePassword.getCacheSize());
        
        servicioCachePassword.encodePassword(password1);
        assertEquals(1, servicioCachePassword.getCacheSize());
        
        servicioCachePassword.encodePassword(password2);
        assertEquals(2, servicioCachePassword.getCacheSize());
    }

    @Test
    void testIsCached_ShouldReturnCorrectStatus() {
        // Arrange
        String password = "testPassword";
        when(passwordEncoder.encode(password)).thenReturn("$2a$10$test.hash.value.here");

        // Act & Assert
        assertFalse(servicioCachePassword.isCached(password));
        
        servicioCachePassword.encodePassword(password);
        assertTrue(servicioCachePassword.isCached(password));
        
        assertFalse(servicioCachePassword.isCached("nonExistentPassword"));
    }

    @Test
    void testMultipleDifferentPasswords_ShouldCacheAll() {
        // Arrange
        String[] passwords = {"pass1", "pass2", "pass3", "pass4", "pass5"};
        for (int i = 0; i < passwords.length; i++) {
            when(passwordEncoder.encode(passwords[i])).thenReturn("$2a$10$encoded" + (i + 1) + ".hash.value.here");
        }

        // Act
        for (String password : passwords) {
            servicioCachePassword.encodePassword(password);
        }

        // Assert
        assertEquals(passwords.length, servicioCachePassword.getCacheSize());
        for (String password : passwords) {
            assertTrue(servicioCachePassword.isCached(password));
        }
    }
} 