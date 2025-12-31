package app.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponseWrapper;

@Slf4j
@Component
@Order(Ordered.LOWEST_PRECEDENCE) // Run after Spring Security to capture the final response
public class RequestResponseLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            
            // Only wrap API requests
            if (httpRequest.getRequestURI().startsWith("/api/")) {
                ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest);
                LoggingResponseWrapper wrappedResponse = new LoggingResponseWrapper(httpResponse);
                
                try {
                    chain.doFilter(wrappedRequest, wrappedResponse);
                    
                    // Ensure the response body is copied back to the original response
                    try {
                        wrappedResponse.copyBodyToResponse();
                    } catch (Exception e) {
                        log.warn("[ERROR] Error copying response body for {}: {}", httpRequest.getRequestURI(), e.getMessage());
                    }
                } catch (Exception e) {
                    log.error("[ERROR] Error in filter chain for {}: {}", httpRequest.getRequestURI(), e.getMessage(), e);
                    throw e;
                }
            } else {
                chain.doFilter(request, response);
            }
        } else {
            chain.doFilter(request, response);
        }
    }
    
    /**
     * Custom response wrapper that captures the response body for logging
     */
    private static class LoggingResponseWrapper extends HttpServletResponseWrapper {
        private final ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
        private final PrintWriter writer = new PrintWriter(new OutputStreamWriter(contentStream, StandardCharsets.UTF_8));
        private final ServletOutputStream outputStream = new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                contentStream.write(b);
                try {
                    getResponse().getOutputStream().write(b);
                } catch (IOException e) {
                    log.warn("[ERROR] Error writing to original response: {}", e.getMessage());
                }
            }
            
            @Override
            public void write(byte[] b) throws IOException {
                contentStream.write(b);
                try {
                    getResponse().getOutputStream().write(b);
                } catch (IOException e) {
                    log.warn("[ERROR] Error writing to original response: {}", e.getMessage());
                }
            }
            
            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                contentStream.write(b, off, len);
                try {
                    getResponse().getOutputStream().write(b, off, len);
                } catch (IOException e) {
                    log.warn("[ERROR] Error writing to original response: {}", e.getMessage());
                }
            }
            
            @Override
            public boolean isReady() {
                try {
                    return getResponse().getOutputStream().isReady();
                } catch (IOException e) {
                    log.warn("[ERROR] Error checking if output stream is ready: {}", e.getMessage());
                    return false;
                }
            }
            
            @Override
            public void setWriteListener(jakarta.servlet.WriteListener writeListener) {
                try {
                    getResponse().getOutputStream().setWriteListener(writeListener);
                } catch (IOException e) {
                    log.warn("[ERROR] Error setting write listener: {}", e.getMessage());
                }
            }
        };
        
        public LoggingResponseWrapper(HttpServletResponse response) {
            super(response);
        }
        
        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            return outputStream;
        }
        
        @Override
        public PrintWriter getWriter() throws IOException {
            return writer;
        }
        
        public byte[] getContentAsByteArray() {
            return contentStream.toByteArray();
        }
        
        public void copyBodyToResponse() throws IOException {
            // The content is already written to the original response
            // Just flush the writer to ensure all content is written
            writer.flush();
        }
    }
} 