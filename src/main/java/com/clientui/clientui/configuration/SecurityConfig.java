package com.clientui.clientui.configuration;

import com.clientui.clientui.filter.HeaderAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@EnableWebSecurity
@Configuration
public class SecurityConfig {

//    @Autowired
//    public void registerAuthProvider(AuthenticationManagerBuilder auth) throws Exception {
//        // Authentification simple en mémoire pour les tests
//        auth.inMemoryAuthentication();
//    }

//    @Bean
//    public WebSecurityCustomizer webSecurityCustomizer() {
//        // On indique à Spring Security d'ignorer complètement les ressources statiques
//        return web -> web.ignoring().requestMatchers(
//                "/webjars/**",   // -nécessaire pour Bootstrap via WebJars
//                "/css/**",
//                "/js/**",
//                "/images/**",
//                "/favicon.ico"
//        );
//    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/webjars/**",   // Bootstrap via WebJars
                                "/css/**",       // CSS
                                "/js/**",        // JavaScript
                                "/images/**",    // Images
                                "/favicon.ico"   // Favicon
                        ).permitAll()
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

                .httpBasic(Customizer.withDefaults())           // Optionnel, si fallback
                .csrf(csrf -> csrf.disable())
                .addFilterBefore(new HeaderAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}

