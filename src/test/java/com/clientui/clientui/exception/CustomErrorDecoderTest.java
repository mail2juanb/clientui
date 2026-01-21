package com.clientui.clientui.exception;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import static org.junit.jupiter.api.Assertions.*;


public class CustomErrorDecoderTest {

    private CustomErrorDecoder customErrorDecoder;
    private Request request;

    @BeforeEach
    void setUp() {
        customErrorDecoder = new CustomErrorDecoder();
        request = Request.create(
                Request.HttpMethod.GET,
                "http://test.com/api",
                new HashMap<>(),
                null,
                StandardCharsets.UTF_8,
                null
        );
    }

    @Test
    void decode_WithValidResponseBody_ShouldReturnFeignException() {
        // Arrange
        String responseBody = "{\"error\": \"Bad Request\"}";
        Response response = Response.builder()
                .status(400)
                .reason("Bad Request")
                .request(request)
                .headers(new HashMap<>())
                .body(responseBody, StandardCharsets.UTF_8)
                .build();

        // Act
        Exception result = customErrorDecoder.decode("TestMethod", response);

        // Assert
        assertNotNull(result);
        assertInstanceOf(FeignException.class, result);
        FeignException feignException = (FeignException) result;
        assertEquals(400, feignException.status());
    }

    @Test
    void decode_WithEmptyResponseBody_ShouldReturnFeignException() {
        // Arrange
        String responseBody = "";
        Response response = Response.builder()
                .status(404)
                .reason("Not Found")
                .request(request)
                .headers(new HashMap<>())
                .body(responseBody, StandardCharsets.UTF_8)
                .build();

        // Act
        Exception result = customErrorDecoder.decode("TestMethod", response);

        // Assert
        assertNotNull(result);
        assertInstanceOf(FeignException.class, result);
        assertEquals(404, ((FeignException) result).status());
    }

    @Test
    void decode_WithNullResponseBody_ShouldHandleGracefully() {
        // Arrange
        Response response = Response.builder()
                .status(500)
                .reason("Internal Server Error")
                .request(request)
                .headers(new HashMap<>())
                .build();

        // Act
        Exception result = customErrorDecoder.decode("TestMethod", response);

        // Assert
        assertNotNull(result);
        assertInstanceOf(FeignException.class, result);
        assertEquals(500, ((FeignException) result).status());
    }

    @Test
    void decode_WithDifferentStatusCodes_ShouldPreserveStatus() {
        // Test multiple status codes
        int[] statusCodes = {400, 401, 403, 404, 500, 502, 503};

        for (int statusCode : statusCodes) {
            // Arrange
            Response response = Response.builder()
                    .status(statusCode)
                    .reason("Error")
                    .request(request)
                    .headers(new HashMap<>())
                    .body("Error body", StandardCharsets.UTF_8)
                    .build();

            // Act
            Exception result = customErrorDecoder.decode("TestMethod", response);

            // Assert
            assertInstanceOf(FeignException.class, result);
            assertEquals(statusCode, ((FeignException) result).status(),
                    "Status code should be preserved for " + statusCode);
        }
    }

    @Test
    void decode_WithJsonResponseBody_ShouldPreserveContent() {
        // Arrange
        String jsonBody = "{\"message\":\"Resource not found\",\"code\":404}";
        Response response = Response.builder()
                .status(404)
                .reason("Not Found")
                .request(request)
                .headers(new HashMap<>())
                .body(jsonBody, StandardCharsets.UTF_8)
                .build();

        // Act
        Exception result = customErrorDecoder.decode("TestMethod#findById(Long)", response);

        // Assert
        assertNotNull(result);
        assertInstanceOf(FeignException.class, result);
        FeignException feignException = (FeignException) result;
        assertEquals(404, feignException.status());
        assertTrue(feignException.contentUTF8().contains("Resource not found"));
    }

    @Test
    void decode_WithLargeResponseBody_ShouldHandleCorrectly() {
        // Arrange
        StringBuilder largeBody = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeBody.append("This is line ").append(i).append(" of the error message.\n");
        }

        Response response = Response.builder()
                .status(400)
                .reason("Bad Request")
                .request(request)
                .headers(new HashMap<>())
                .body(largeBody.toString(), StandardCharsets.UTF_8)
                .build();

        // Act
        Exception result = customErrorDecoder.decode("TestMethod", response);

        // Assert
        assertNotNull(result);
        assertInstanceOf(FeignException.class, result);
        FeignException feignException = (FeignException) result;
        assertEquals(400, feignException.status());
        assertNotNull(feignException.contentUTF8());
    }

    @Test
    void decode_ShouldRebuildResponseWithBody() {
        // Arrange
        String originalBody = "Original error message";
        Response response = Response.builder()
                .status(400)
                .reason("Bad Request")
                .request(request)
                .headers(new HashMap<>())
                .body(originalBody, StandardCharsets.UTF_8)
                .build();

        // Act
        Exception result = customErrorDecoder.decode("TestMethod", response);

        // Assert
        assertInstanceOf(FeignException.class, result);
        FeignException feignException = (FeignException) result;
        String bodyContent = feignException.contentUTF8();
        assertEquals(originalBody, bodyContent);
    }

    @Test
    void decode_WithIOException_ShouldLogErrorAndReturnFeignException() {
        // Arrange
        java.io.InputStream errorStream = new java.io.InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Read error");
            }
        };

        Response response = Response.builder()
                .status(503)
                .reason("Service Unavailable")
                .request(request)
                .headers(new HashMap<>())
                .body(errorStream, 100)
                .build();

        // Act
        Exception result = customErrorDecoder.decode("TestMethod", response);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof FeignException);
        assertEquals(503, ((FeignException) result).status());
    }

}
