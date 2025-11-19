package com.clientui.clientui.filter;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class AuthHeadersFilter implements Filter {
    // filtre pour extraire les headers (webflux) et les stocker dans un objet accessible aux controleurs

    private static final Logger logger = LoggerFactory.getLogger(AuthHeadersFilter.class);


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        logger.info("=== AuthHeadersFilter: Début du filtre ===");

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        logger.info("Request URL: " + httpRequest.getRequestURL().toString());
        logger.info("Request URI: " + httpRequest.getRequestURI());

        // Lire les headers
        String username = httpRequest.getHeader("X-Auth-Username");
        String roles = httpRequest.getHeader("X-Auth-Roles");
        logger.info("X-Auth-Username: " + username); // Log des headers
        logger.info("X-Auth-Roles: " + roles);

        // Stocker dans un attribut de requête
        if (username != null) {
            httpRequest.setAttribute("userConnected", username);
            httpRequest.setAttribute("userRole", roles);
            logger.info("Attributs définis: userConnected=" + username + ", userRole=" + roles);
        }

        // Continuer la chaîne de filtres
        chain.doFilter(request, response);
        logger.info("=== AuthHeadersFilter: Fin du filtre ===");
    }


}

