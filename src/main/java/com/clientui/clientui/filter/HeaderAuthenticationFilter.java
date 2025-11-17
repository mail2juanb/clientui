package com.clientui.clientui.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(HeaderAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String username = request.getHeader("X-Auth-Username");
        String roles = request.getHeader("X-Auth-Roles");

        logger.info("=== HeaderAuthenticationFilter ===");
        logger.info("Request URI: {}", request.getRequestURI());
        logger.info("X-Auth-Username: {}", username);
        logger.info("X-Auth-Roles: {}", roles);

        if (username != null) {
            List<GrantedAuthority> authorities = new ArrayList<>();
            if (roles != null) {
                String[] roleArray = roles.replace("[", "").replace("]", "").split(",");
                for (String role : roleArray) {
                    String trimmedRole = role.trim();
                    logger.info("Adding role: {}", trimmedRole);
                    authorities.add(new SimpleGrantedAuthority(trimmedRole));
                }
            }

            Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            logger.info("Authentication set for user: {}", username);
            logger.info("Authorities: {}", authorities);

        } else {
            logger.warn("No X-Auth-Username header found!");
        }

        filterChain.doFilter(request, response);
    }
}
