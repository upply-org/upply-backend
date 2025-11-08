package com.upply.security;

import io.jsonwebtoken.JwtException;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;


    @Override
    protected void doFilterInternal(
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain) throws ServletException, IOException
    {

        // skip authentication and call the rest of the filter chain; user stays unauthenticated.
        if(request.getServletPath().contains("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }




        // header field of the http request ===> (Authorization: Bearer eyJhbGciOiJIUzI1Ni...)
        final String authorizationHeader = request.getHeader("Authorization");

        // skip authentication and call the rest of the filter chain; user stays unauthenticated.
        if(authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }




        final String jwtToken = authorizationHeader.substring(7);

        try {
            final String userEmail = jwtService.extractUsername(jwtToken);

            // Check If User Is Not Already Authenticated => we proceed to authenticate using the token
            if(userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                if(jwtService.isTokenValid(jwtToken, userDetails)) {

                    // Credentials (null) ===> We’re not authenticating with a password now
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    // Optional: This line adds extra context (metadata) about the current HTTP request
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // The current user for this request is this authenticated user
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }
        catch (JwtException ex) {
            filterChain.doFilter(request, response);
            return;
        }




        // Call the rest of the filter chain
        filterChain.doFilter(request, response);
    }
}
