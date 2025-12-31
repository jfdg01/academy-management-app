package app.config;

import app.servicios.ServicioJwt;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class ConfigFiltroJwt extends OncePerRequestFilter {

    private final ServicioJwt servicioJwt;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            jwt = authHeader.substring(7);
            username = servicioJwt.extractUsername(jwt);
            
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                    
                    if (servicioJwt.isTokenValid(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        authToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
                    // User not found in database, but token might be valid
                    // Log the issue but don't throw an exception
                    // This allows the request to continue without authentication
                    System.out.println("Warning: JWT token contains username '" + username + "' that doesn't exist in database");
                }
            }
        } catch (Exception e) {
            // Log any other JWT-related errors but don't throw exceptions
            // This prevents the filter from breaking the request chain
            System.out.println("Warning: Error processing JWT token: " + e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }
} 