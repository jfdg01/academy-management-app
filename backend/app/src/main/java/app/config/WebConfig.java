package app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final ApiLoggingInterceptor apiLoggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // During development, you may want to include auth endpoints for debugging.
        // Comment out the excludePathPatterns line below to log /api/auth/** as well.
        registry.addInterceptor(apiLoggingInterceptor)
                .addPathPatterns("/api/**");
                // .excludePathPatterns("/api/auth/**"); // Exclude in production to avoid logging sensitive data
    }
} 