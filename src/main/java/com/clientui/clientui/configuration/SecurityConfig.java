package com.clientui.clientui.configuration;

import com.clientui.clientui.filter.HeaderAuthenticationFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Autowired
    public void registerAuthProvider(AuthenticationManagerBuilder auth) throws Exception {
        // Authentification simple en mémoire pour les tests
        auth.inMemoryAuthentication();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // On indique à Spring Security d'ignorer complètement les ressources statiques
        return web -> web.ignoring().requestMatchers(
                "/webjars/**",   // -nécessaire pour Bootstrap via WebJars
                "/css/**",
                "/js/**",
                "/images/**",
                "/favicon.ico"
        );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        //.requestMatchers("/home").permitAll()
                        .requestMatchers("/webjars/**").permitAll()
                        .anyRequest().authenticated()
                )

//                                .requestMatchers(HttpMethod.GET, "/books")
//                                .permitAll()
//
//                                .requestMatchers(HttpMethod.GET, "/books/*")
//                                .permitAll()
//
//                                .requestMatchers(HttpMethod.POST, "/books")
//                                .hasRole("ADMIN")
//
//                                .requestMatchers(HttpMethod.PATCH, "/books/*")
//                                .hasRole("ADMIN")
//
//                                .requestMatchers(HttpMethod.DELETE, "/books/*")
//                                .hasRole("ADMIN")

                .httpBasic(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .addFilterBefore(new HeaderAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}

