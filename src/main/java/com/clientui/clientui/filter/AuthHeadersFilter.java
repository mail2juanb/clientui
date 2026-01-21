package com.clientui.clientui.filter;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Servlet filter responsible for extracting authentication headers (e.g., {@code X-Auth-Username},
 * {@code X-Auth-Roles}) from incoming HTTP requests and storing them as request attributes.
 * These attributes are later accessible to controllers for rendering user-specific information
 * (e.g., username, role) in the UI.
 *
 * <p>This filter is part of the authentication flow in the *MicroDiab* project and ensures that
 * user context is propagated throughout the request lifecycle. It is particularly useful for
 * displaying the connected user's name and role in the frontend (e.g., Thymeleaf templates).</p>
 *
 * <p>This filter does not perform authentication itself but prepares the request attributes
 * for further processing by other filters or controllers.</p>
 *
 * @see Filter
 * @see HttpServletRequest
 */
public class AuthHeadersFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(AuthHeadersFilter.class);

    /**
     * Extracts the {@code X-Auth-Username} and {@code X-Auth-Roles} headers from the incoming request
     * and stores them as request attributes ({@code userConnected} and {@code userRole}).
     * These attributes can then be accessed by controllers to display user-specific information.
     *
     * <p>If the headers are missing, no attributes are set, and the request continues normally.</p>
     *
     * @param request  The {@link ServletRequest} object, cast to {@link HttpServletRequest} to access headers.
     * @param response The {@link ServletResponse} object.
     * @param chain    The {@link FilterChain} object to continue the request processing.
     * @throws IOException      If an I/O error occurs during request processing.
     * @throws ServletException If a servlet-related error occurs.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        logger.debug("=== AuthHeadersFilter: Début du filtre ===");

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        logger.debug("Request URL: " + httpRequest.getRequestURL().toString());
        logger.debug("Request URI: " + httpRequest.getRequestURI());

        // Read the headers
        String username = httpRequest.getHeader("X-Auth-Username");
        String roles = httpRequest.getHeader("X-Auth-Roles");
        logger.debug("X-Auth-Username: " + username); // Log des headers
        logger.debug("X-Auth-Roles: " + roles);

        // Store in a query attribute
        if (username != null) {
            httpRequest.setAttribute("userConnected", username);
            httpRequest.setAttribute("userRole", roles);
            logger.debug("Defined attributes: userConnected=" + username + ", userRole=" + roles);
        }

        // Continue the filter chain
        chain.doFilter(request, response);
        logger.debug("=== AuthHeadersFilter: Fin du filtre ===");
    }


}

