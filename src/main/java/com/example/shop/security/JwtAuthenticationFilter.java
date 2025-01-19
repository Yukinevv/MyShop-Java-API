package com.example.shop.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsServiceImpl userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 1. Brak nagłówka lub nie zaczyna się od "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return; // nic nie robimy, przepuszczamy dalej
        }

        // 2. Wyciągamy token JWT
        jwt = authHeader.substring(7); // "Bearer " ma 7 znaków
        username = jwtService.extractUsername(jwt);

        // 3. Jeśli user jeszcze nie jest zalogowany w SecurityContext
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 4. Pobieramy dane użytkownika (Spring Security userdetails)
            var userDetails = userDetailsService.loadUserByUsername(username);

            // 5. Weryfikujemy ważność tokenu
            if (jwtService.isTokenValid(jwt, userDetails.getUsername())) {
                // 6. Tworzymy obiekt Authentication i ustawiamy w kontekście
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
        }

        // kontynuujemy chain
        filterChain.doFilter(request, response);
    }
}
