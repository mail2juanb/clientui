package com.clientui.clientui.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class HeaderAuthenticationFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private HeaderAuthenticationFilter headerAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_WithUsernameAndRoles_ShouldSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("X-Auth-Username")).thenReturn("testUser");
        when(request.getHeader("X-Auth-Roles")).thenReturn("[ROLE_USER, ROLE_ADMIN]");

        // Act
        headerAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("testUser", authentication.getName());
        assertEquals(2, authentication.getAuthorities().size());

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithUsernameAndNoRoles_ShouldSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("X-Auth-Username")).thenReturn("testUser");
        when(request.getHeader("X-Auth-Roles")).thenReturn(null);

        // Act
        headerAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("testUser", authentication.getName());
        assertTrue(authentication.getAuthorities().isEmpty());

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithNoUsername_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("X-Auth-Username")).thenReturn(null);
        when(request.getHeader("X-Auth-Roles")).thenReturn("[ROLE_USER]");

        // Act
        headerAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void shouldNotFilter_WithWebJarsPath_ShouldReturnTrue() throws ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/webjars/bootstrap/5.1.3/css/bootstrap.min.css");

        // Act
        boolean result = headerAuthenticationFilter.shouldNotFilter(request);

        // Assert
        assertTrue(result, "Webjars path should be excluded from filtering");
    }

    @Test
    void shouldNotFilter_WithCssPath_ShouldReturnTrue() throws ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/css/styles.css");

        // Act
        boolean result = headerAuthenticationFilter.shouldNotFilter(request);

        // Assert
        assertTrue(result, "CSS path should be excluded from filtering");
    }

    @Test
    void shouldNotFilter_WithFaviconPath_ShouldReturnTrue() throws ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/favicon.ico");

        // Act
        boolean result = headerAuthenticationFilter.shouldNotFilter(request);

        // Assert
        assertTrue(result, "Favicon path should be excluded from filtering");
    }

    @Test
    void shouldNotFilter_WithApiPath_ShouldReturnFalse() throws ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/users");

        // Act
        boolean result = headerAuthenticationFilter.shouldNotFilter(request);

        // Assert
        assertFalse(result, "API path should not be excluded from filtering");
    }

    @Test
    void shouldNotFilter_WithRootPath_ShouldReturnFalse() throws ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/");

        // Act
        boolean result = headerAuthenticationFilter.shouldNotFilter(request);

        // Assert
        assertFalse(result, "Root path should not be excluded from filtering");
    }

    @Test
    void shouldNotFilter_WithOtherStaticResource_ShouldReturnFalse() throws ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/images/logo.png");

        // Act
        boolean result = headerAuthenticationFilter.shouldNotFilter(request);

        // Assert
        assertFalse(result, "Other static resources should not be excluded from filtering");
    }

    @Test
    void shouldNotFilter_WithActuatorPath_ShouldReturnTrue() throws ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/actuator/health");

        // Act
        boolean result = headerAuthenticationFilter.shouldNotFilter(request);

        // Assert
        assertTrue(result, "Actuator path should be excluded from filtering");
    }

}

