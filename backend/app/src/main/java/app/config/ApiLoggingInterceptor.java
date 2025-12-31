package app.config;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.servicios.ServicioJwt;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiLoggingInterceptor implements HandlerInterceptor {

    private final ServicioJwt servicioJwt;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    
    // ANSI Color codes for console output
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLUE = "\u001B[34m";      // Request phase
    private static final String ANSI_GREEN = "\u001B[32m";     // Response phase (success)
    private static final String ANSI_RED = "\u001B[31m";       // Response phase (error)
    private static final String ANSI_YELLOW = "\u001B[33m";    // Request ID
    private static final String ANSI_CYAN = "\u001B[36m";      // API-LOG tag
    private static final String ANSI_PURPLE = "\u001B[35m";    // Response body
    private static final String ANSI_WHITE = "\u001B[37m";     // Separators
    
    // Configurable properties for response body logging
    @Value("${app.logging.response-body.enabled:true}")
    private boolean responseBodyLoggingEnabled;
    
    @Value("${app.logging.response-body.max-length:2000}")
    private int maxResponseBodyLength;
    
    @Value("${app.logging.colors.enabled:true}")
    private boolean colorsEnabled;
    
    @Value("${app.logging.encapsulation.enabled:true}")
    private boolean encapsulationEnabled;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Generate unique request ID for correlation
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        request.setAttribute("requestId", requestId);
        request.setAttribute("startTime", System.currentTimeMillis());

        // Log request start separator
        if (encapsulationEnabled) {
            log.info("{}", colorize(ANSI_WHITE, "╔══════════════════════════════════════════════════════════════════════════════"));
        }

        // Extract user info from JWT if available
        String userInfo = extractUserInfo(request);
        
        // Log request details with condensed format
        log.info("{}{}{} {}[API-LOG]{} {}>>> {} {} | User: {}{}", 
                colorize(ANSI_YELLOW, requestId),
                ANSI_RESET,
                " ",
                colorize(ANSI_CYAN, ""),
                ANSI_RESET,
                colorize(ANSI_BLUE, ""),
                request.getMethod(), 
                request.getRequestURI(),
                userInfo,
                ANSI_RESET);

        // Log additional user context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getName())) {
            
            Object principal = authentication.getPrincipal();
            if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
                log.info("{}{}{} {}[API-LOG]{} User Context: {} | Enabled: {} | Authorities: {}", 
                    colorize(ANSI_YELLOW, requestId), ANSI_RESET, " ",
                    colorize(ANSI_CYAN, ""), ANSI_RESET,
                    userDetails.getUsername(),
                    userDetails.isEnabled(),
                    userDetails.getAuthorities());
            }
        }

        // Log request body for all operations that might have a body
        String requestBody = getRequestBody(request);
        if (requestBody != null && !requestBody.trim().isEmpty()) {
            log.info("{}{}{} {}[API-LOG]{} Request JSON: {}", 
                colorize(ANSI_YELLOW, requestId), ANSI_RESET, " ",
                colorize(ANSI_CYAN, ""), ANSI_RESET,
                maskSensitiveData(requestBody));
        }

        // Log query parameters if present
        String queryString = request.getQueryString();
        if (queryString != null && !queryString.trim().isEmpty()) {
            log.info("{}{}{} {}[API-LOG]{} Query Parameters: {}", 
                colorize(ANSI_YELLOW, requestId), ANSI_RESET, " ",
                colorize(ANSI_CYAN, ""), ANSI_RESET,
                queryString);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String requestId = (String) request.getAttribute("requestId");
        Long startTime = (Long) request.getAttribute("startTime");
        
        if (requestId != null && startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            
            // Log response details with condensed format
            String responseColor = response.getStatus() >= 400 ? ANSI_RED : ANSI_GREEN;
            log.info("{}{}{} {}[API-LOG]{} {}<<< {} {} | User: {} | Duration: {}ms | Status: {}{}", 
                    colorize(ANSI_YELLOW, requestId), ANSI_RESET, " ",
                    colorize(ANSI_CYAN, ""), ANSI_RESET,
                    colorize(responseColor, ""),
                    request.getMethod(), 
                    request.getRequestURI(),
                    extractUserInfo(request),
                    duration,
                    response.getStatus(),
                    ANSI_RESET);

            // Enhanced response body logging
            if (responseBodyLoggingEnabled) {
                logResponseBody(response, requestId);
            }

            // Log exception if present
            if (ex != null) {
                log.error("{}{}{} {}[API-LOG]{} {}[ERROR] Exception occurred: {}{}", 
                    colorize(ANSI_YELLOW, requestId), ANSI_RESET, " ",
                    colorize(ANSI_CYAN, ""), ANSI_RESET,
                    colorize(ANSI_RED, ""), ex.getMessage(), ANSI_RESET, ex);
            }
            
            // Log request end separator
            if (encapsulationEnabled) {
                log.info("{}", colorize(ANSI_WHITE, "╚══════════════════════════════════════════════════════════════════════════════"));
            }
        }
    }

    private String extractUserInfo(HttpServletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getName())) {
                
                // Get the principal object to extract more user details
                Object principal = authentication.getPrincipal();
                String userInfo = authentication.getName();
                
                // Try to extract additional user details
                if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
                    // Extract role information
                    String roles = userDetails.getAuthorities().stream()
                        .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                        .findFirst()
                        .orElse("UNKNOWN");
                    
                    userInfo = String.format("%s (Role: %s)", userInfo, roles);
                }
                
                return userInfo;
            }
            
            // Try to extract from JWT header if not in security context
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7);
                String username = servicioJwt.extractUsername(jwt);
                if (username != null && !username.isEmpty()) {
                    return username + " (JWT)";
                }
            }
        } catch (Exception e) {
            // Silently handle JWT parsing errors
        }
        return "anonymous";
    }

    /**
     * Retrieves the request body from the given HttpServletRequest if it is wrapped
     * in a ContentCachingRequestWrapper. This is useful for logging or inspecting
     * the request payload after it has been read by the servlet. If the request body
     * is available, it returns it as a UTF-8 encoded string; otherwise, returns null.
     * If an error occurs while reading the body, logs the error at debug level.
     *
     * @param request the HttpServletRequest to extract the body from
     * @return the request body as a String, or null if not available
     */
    private String getRequestBody(HttpServletRequest request) {
        try {
            if (request instanceof ContentCachingRequestWrapper wrapper) {
                byte[] content = wrapper.getContentAsByteArray();
                if (content.length > 0) {
                    return new String(content, StandardCharsets.UTF_8);
                }
            }
        } catch (Exception e) {
            log.debug("[ERROR] Could not read request body: {}", e.getMessage());
        }
        return null;
    }

    private String getResponseBody(HttpServletResponse response) {
        try {
            // Try to get response body from different wrapper types
            if (response instanceof ContentCachingResponseWrapper wrapper) {
                byte[] content = wrapper.getContentAsByteArray();
                
                if (content.length > 0) {
                    String responseBody = new String(content, StandardCharsets.UTF_8);
                    return responseBody;
                }
            } else if (response.getClass().getName().contains("LoggingResponseWrapper")) {
                // Try to access our custom wrapper using reflection
                try {
                    java.lang.reflect.Method getContentMethod = response.getClass().getMethod("getContentAsByteArray");
                    byte[] content = (byte[]) getContentMethod.invoke(response);
                    if (content != null && content.length > 0) {
                        String responseBody = new String(content, StandardCharsets.UTF_8);
                        return responseBody;
                    }
                } catch (Exception e) {
                    // Silently handle reflection errors
                }
            }
        } catch (Exception e) {
            // Silently handle response body reading errors
        }
        return null;
    }

    private String maskSensitiveData(String content) {
        // Enhanced masking for sensitive fields
        return content.replaceAll("(\"password\"\\s*:\\s*\")([^\"]*)(\")", "$1***$3")
                     .replaceAll("(\"token\"\\s*:\\s*\")([^\"]*)(\")", "$1***$3")
                     .replaceAll("(\"secret\"\\s*:\\s*\")([^\"]*)(\")", "$1***$3")
                     .replaceAll("(\"authorization\"\\s*:\\s*\")([^\"]*)(\")", "$1***$3")
                     .replaceAll("(\"jwt\"\\s*:\\s*\")([^\"]*)(\")", "$1***$3");
    }

    /**
     * Enhanced response body logging with better error handling and formatting
     */
    private void logResponseBody(HttpServletResponse response, String requestId) {
        try {
            String responseBody = getResponseBody(response);
            
            if (responseBody != null && !responseBody.trim().isEmpty()) {
                // Check if response is JSON and try to format it
                String contentType = response.getContentType();
                
                if (contentType != null && contentType.contains("application/json")) {
                    try {
                        // Pretty print JSON for better readability
                        Object jsonObject = objectMapper.readValue(responseBody, Object.class);
                        String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
                        
                        if (prettyJson.length() > maxResponseBodyLength) {
                            log.info("{}{}{} {}[API-LOG]{} {}Response (truncated):{}\n{}...", 
                                colorize(ANSI_YELLOW, requestId), ANSI_RESET, " ",
                                colorize(ANSI_CYAN, ""), ANSI_RESET,
                                colorize(ANSI_PURPLE, ""), ANSI_RESET,
                                prettyJson.substring(0, maxResponseBodyLength));
                        } else {
                            log.info("{}{}{} {}[API-LOG]{} {}Response:{}\n{}", 
                                colorize(ANSI_YELLOW, requestId), ANSI_RESET, " ",
                                colorize(ANSI_CYAN, ""), ANSI_RESET,
                                colorize(ANSI_PURPLE, ""), ANSI_RESET,
                                prettyJson);
                        }
                    } catch (Exception jsonException) {
                        // If JSON parsing fails, log as plain text
                        logResponseBodyAsText(responseBody, requestId, "JSON parsing failed, logging as text");
                    }
                } else {
                    // Non-JSON response
                    logResponseBodyAsText(responseBody, requestId, "Non-JSON response");
                }
            }
        } catch (Exception e) {
            log.warn("{}{}{} {}[API-LOG]{} {}[ERROR] Could not log response body: {}{}", 
                colorize(ANSI_YELLOW, requestId), ANSI_RESET, " ",
                colorize(ANSI_CYAN, ""), ANSI_RESET,
                colorize(ANSI_RED, ""), e.getMessage(), ANSI_RESET);
        }
    }

    /**
     * Log response body as plain text with truncation
     */
    private void logResponseBodyAsText(String responseBody, String requestId, String context) {
        if (responseBody.length() > maxResponseBodyLength) {
            log.info("{}{}{} {}[API-LOG]{} {}Response ({} - truncated):{} {}...", 
                colorize(ANSI_YELLOW, requestId), ANSI_RESET, " ",
                colorize(ANSI_CYAN, ""), ANSI_RESET,
                colorize(ANSI_PURPLE, ""), context, ANSI_RESET,
                responseBody.substring(0, maxResponseBodyLength));
        } else {
            log.info("{}{}{} {}[API-LOG]{} {}Response ({}):{} {}", 
                colorize(ANSI_YELLOW, requestId), ANSI_RESET, " ",
                colorize(ANSI_CYAN, ""), ANSI_RESET,
                colorize(ANSI_PURPLE, ""), context, ANSI_RESET, responseBody);
        }
    }

    /**
     * Helper method to apply colors only if colors are enabled
     */
    private String colorize(String color, String text) {
        return colorsEnabled ? color + text : text;
    }
} 