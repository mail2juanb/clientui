package com.clientui.clientui.filter;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;


public class AuthHeadersFilter implements Filter {
    // filtre pour extraire les headers (webflux) et les stocker dans un objet accessible aux controleurs

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        System.out.println("=== AuthHeadersFilter: Début du filtre ==="); // Log de début
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        System.out.println("Request URL: " + httpRequest.getRequestURL().toString()); // <-- Ajoute cette ligne
        System.out.println("Request URI: " + httpRequest.getRequestURI()); // <-- Optionnel, pour plus de détails

        // Lire les headers
        String username = httpRequest.getHeader("X-Auth-Username");
        String roles = httpRequest.getHeader("X-Auth-Roles");
        System.out.println("X-Auth-Username: " + username); // Log des headers
        System.out.println("X-Auth-Roles: " + roles);

        // Stocker dans un attribut de requête
        if (username != null) {
            httpRequest.setAttribute("userConnected", username);
            httpRequest.setAttribute("userRole", roles);
            System.out.println("Attributs définis: userConnected=" + username + ", userRole=" + roles);
        }

        // Continuer la chaîne de filtres
        chain.doFilter(request, response);
        System.out.println("=== AuthHeadersFilter: Fin du filtre ==="); // Log de fin
    }


}

