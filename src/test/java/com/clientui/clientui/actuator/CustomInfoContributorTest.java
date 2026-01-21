package com.clientui.clientui.actuator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.info.Info;
import org.springframework.core.env.Environment;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomInfoContributorTest {

    @Mock
    private Environment environment;

    @InjectMocks
    private CustomInfoContributor customInfoContributor;

    @Test
    void contribute_ShouldAddAppInfoToBuilder() {
        // Arrange
        when(environment.getProperty("info.app.version", "clientui - Version not defined")).thenReturn("1.0.0");
        when(environment.getProperty("info.app.description", "clientui - Description not defined")).thenReturn("Gestion des patients");
        when(environment.getProperty("info.app.documentation.javadoc", "clientui - Javadoc Documentation not defined")).thenReturn("https://docs.microdiab.com/clientui/javadoc");
        when(environment.getProperty("info.app.information", "clientui - Informations not defined")).thenReturn("Microservice de gestion des patients");

        Info.Builder builder = new Info.Builder();

        // Act
        customInfoContributor.contribute(builder);

        // Assert
        Info info = builder.build();
        Map<String, Object> details = info.getDetails();

        assertNotNull(details);
        assertTrue(details.containsKey("app"));

        @SuppressWarnings("unchecked")
        Map<String, Object> appInfo = (Map<String, Object>) details.get("app");

        assertEquals("1.0.0", appInfo.get("version"));
        assertEquals("Gestion des patients", appInfo.get("description"));
        assertEquals("https://docs.microdiab.com/clientui/javadoc", appInfo.get("documentation-javadoc"));
        assertEquals("Microservice de gestion des patients", appInfo.get("information"));
        assertNotNull(appInfo.get("lastUpdated"));
    }

    @Test
    void contribute_ShouldUseDefaultValuesIfPropertiesAreMissing() {
        // Arrange
        when(environment.getProperty("info.app.version", "clientui - Version not defined")).thenReturn("clientui - Version not defined");
        when(environment.getProperty("info.app.description", "clientui - Description not defined")).thenReturn("clientui - Description not defined");
        when(environment.getProperty("info.app.documentation.javadoc", "clientui - Javadoc Documentation not defined")).thenReturn("clientui - Javadoc Documentation not defined");
        when(environment.getProperty("info.app.information", "clientui - Informations not defined")).thenReturn("clientui - Informations not defined");

        Info.Builder builder = new Info.Builder();

        // Act
        customInfoContributor.contribute(builder);

        // Assert
        Info info = builder.build();
        Map<String, Object> details = info.getDetails();

        assertNotNull(details);
        assertTrue(details.containsKey("app"));

        @SuppressWarnings("unchecked")
        Map<String, Object> appInfo = (Map<String, Object>) details.get("app");

        assertEquals("clientui - Version not defined", appInfo.get("version"));
        assertEquals("clientui - Description not defined", appInfo.get("description"));
        assertEquals("clientui - Javadoc Documentation not defined", appInfo.get("documentation-javadoc"));
        assertEquals("clientui - Informations not defined", appInfo.get("information"));
        assertNotNull(appInfo.get("lastUpdated"));
    }

}