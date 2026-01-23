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

/**
 * Spring Security filter that authenticates users based on custom HTTP headers
 * ({@code X-Auth-Username} and {@code X-Auth-Roles}).
 * This filter creates a Spring Security {@link Authentication} object and sets it in the
 * {@link SecurityContextHolder} for the current request.
 *
 * <p>Static resources (e.g., CSS, webjars, favicon) are excluded from this filter to avoid
 * authentication issues during resource loading. This is critical to prevent MIME type errors
 * and ensure proper rendering of static assets.</p>
 *
 * <p>This filter is part of the *MicroDiab* project's security architecture, working in tandem
 * with the *mgateway* microservice for authentication and role management.</p>
 *
 * @see OncePerRequestFilter
 * @see Authentication
 * @see SecurityContextHolder
 */
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    /** Logger for this class. */
    private static final Logger logger = LoggerFactory.getLogger(HeaderAuthenticationFilter.class);

    /**
     * Determines whether this filter should be applied to the current request.
     * Static resources (e.g., CSS, webjars, favicon, actuator) are excluded to avoid breaking the UI.
     *
     * @param request The current {@link HttpServletRequest}.
     * @return {@code true} if the request should be excluded from filtering, {@code false} otherwise.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/webjars/") || path.startsWith("/css/") || path.equals("/favicon.ico") || path.startsWith("/actuator/");
    }

    /**
     * Extracts the {@code X-Auth-Username} and {@code X-Auth-Roles} headers from the request,
     * creates a Spring Security {@link Authentication} object, and sets it in the
     * {@link SecurityContextHolder}.
     *
     * <p>If the headers are missing, a warning is logged, and the request continues without
     * authentication.</p>
     *
     * @param request     The current {@link HttpServletRequest}.
     * @param response    The current {@link HttpServletResponse}.
     * @param filterChain The {@link FilterChain} to continue request processing.
     * @throws ServletException If a servlet-related error occurs.
     * @throws IOException      If an I/O error occurs.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String username = request.getHeader("X-Auth-Username");
        String roles = request.getHeader("X-Auth-Roles");

        logger.debug("=== HeaderAuthenticationFilter ===");
        logger.debug("Request URI: {}", request.getRequestURI());
        logger.debug("X-Auth-Username: {}", username);
        logger.debug("X-Auth-Roles: {}", roles);

        if (username != null) {
            List<GrantedAuthority> authorities = new ArrayList<>();
            if (roles != null) {
                String[] roleArray = roles.replace("[", "").replace("]", "").split(",");
                for (String role : roleArray) {
                    String trimmedRole = role.trim();
                    logger.debug("Adding role: {}", trimmedRole);
                    authorities.add(new SimpleGrantedAuthority(trimmedRole));
                }
            }

            Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            logger.debug("Authentication set for user: {}", username);
            logger.debug("Authorities: {}", authorities);

        } else {
            String path = request.getRequestURI();
            logger.warn("No X-Auth-Username header found in path : {}", path);
        }

        filterChain.doFilter(request, response);
    }
}
