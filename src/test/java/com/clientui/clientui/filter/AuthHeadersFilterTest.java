package com.clientui.clientui.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.ServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthHeadersFilterTest {

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private ServletResponse response;

    @Mock
    private FilterChain chain;

    @InjectMocks
    private AuthHeadersFilter authHeadersFilter;

    @Test
    void doFilter_WithUsernameAndRoles_ShouldSetRequestAttributes() throws ServletException, IOException, MalformedURLException {
        // Arrange
        when(httpRequest.getHeader("X-Auth-Username")).thenReturn("testUser");
        when(httpRequest.getHeader("X-Auth-Roles")).thenReturn("[ROLE_USER, ROLE_ADMIN]");
        when(httpRequest.getRequestURL()).thenReturn(new StringBuffer("http://example.com/test"));
        when(httpRequest.getRequestURI()).thenReturn("/test");

        // Act
        authHeadersFilter.doFilter(httpRequest, response, chain);

        // Assert
        verify(httpRequest, times(1)).setAttribute("userConnected", "testUser");
        verify(httpRequest, times(1)).setAttribute("userRole", "[ROLE_USER, ROLE_ADMIN]");
        verify(chain, times(1)).doFilter(httpRequest, response);
    }

    @Test
    void doFilter_WithUsernameAndNoRoles_ShouldSetRequestAttributes() throws ServletException, IOException, MalformedURLException {
        // Arrange
        when(httpRequest.getHeader("X-Auth-Username")).thenReturn("testUser");
        when(httpRequest.getHeader("X-Auth-Roles")).thenReturn(null);
        when(httpRequest.getRequestURL()).thenReturn(new StringBuffer("http://example.com/test"));
        when(httpRequest.getRequestURI()).thenReturn("/test");

        // Act
        authHeadersFilter.doFilter(httpRequest, response, chain);

        // Assert
        verify(httpRequest, times(1)).setAttribute("userConnected", "testUser");
        verify(httpRequest, times(1)).setAttribute("userRole", null);
        verify(chain, times(1)).doFilter(httpRequest, response);
    }

    @Test
    void doFilter_WithNoUsername_ShouldNotSetRequestAttributes() throws ServletException, IOException, MalformedURLException {
        // Arrange
        when(httpRequest.getHeader("X-Auth-Username")).thenReturn(null);
        when(httpRequest.getHeader("X-Auth-Roles")).thenReturn("[ROLE_USER]");
        when(httpRequest.getRequestURL()).thenReturn(new StringBuffer("http://example.com/test"));
        when(httpRequest.getRequestURI()).thenReturn("/test");

        // Act
        authHeadersFilter.doFilter(httpRequest, response, chain);

        // Assert
        verify(httpRequest, never()).setAttribute(eq("userConnected"), any());
        verify(httpRequest, never()).setAttribute(eq("userRole"), any());
        verify(chain, times(1)).doFilter(httpRequest, response);
    }
}
