package app.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import app.servicios.ServicioJwt;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class ApiLoggingInterceptorTest {

    @Mock
    private ServicioJwt servicioJwt;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    private ApiLoggingInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new ApiLoggingInterceptor(servicioJwt);
        
        // Setup basic request/response mocks
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/test");
        
        // Setup security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testPreHandle_ShouldSetRequestAttributes() throws Exception {
        // Act
        boolean result = interceptor.preHandle(request, response, null);

        // Assert
        assert result;
        verify(request).setAttribute(eq("requestId"), anyString());
        verify(request).setAttribute(eq("startTime"), anyLong());
    }

    @Test
    void testAfterCompletion_ShouldLogResponse() throws Exception {
        // Arrange
        when(request.getAttribute("requestId")).thenReturn("12345678");
        when(request.getAttribute("startTime")).thenReturn(System.currentTimeMillis() - 100);
        when(response.getStatus()).thenReturn(200);

        // Act
        interceptor.afterCompletion(request, response, null, null);

        // Assert
        // The method should complete without throwing exceptions
    }

    @Test
    void testAfterCompletion_WithError_ShouldLogErrorResponse() throws Exception {
        // Arrange
        when(request.getAttribute("requestId")).thenReturn("12345678");
        when(request.getAttribute("startTime")).thenReturn(System.currentTimeMillis() - 100);
        when(response.getStatus()).thenReturn(400);

        // Act
        interceptor.afterCompletion(request, response, null, null);

        // Assert
        // The method should complete without throwing exceptions
    }

} 