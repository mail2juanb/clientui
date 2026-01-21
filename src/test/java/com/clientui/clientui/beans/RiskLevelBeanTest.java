package com.clientui.clientui.beans;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class RiskLevelBeanTest {

    @Test
    void testDefaultConstructor() {
        // Arrange & Act
        RiskLevelBean riskLevelBean = new RiskLevelBean();

        // Assert
        assertThat(riskLevelBean).isNotNull();
        assertThat(riskLevelBean.getPatId()).isNull();
        assertThat(riskLevelBean.getRiskLevel()).isNull();
    }

    @Test
    void testParameterizedConstructor() {
        // Arrange
        Long patId = 123L;
        String riskLevel = "High";

        // Act
        RiskLevelBean riskLevelBean = new RiskLevelBean(riskLevel, patId);

        // Assert
        assertThat(riskLevelBean).isNotNull();
        assertThat(riskLevelBean.getPatId()).isEqualTo(patId);
        assertThat(riskLevelBean.getRiskLevel()).isEqualTo(riskLevel);
    }

    @Test
    void testSetAndGetPatId() {
        // Arrange
        RiskLevelBean riskLevelBean = new RiskLevelBean();
        Long patId = 456L;

        // Act
        riskLevelBean.setPatId(patId);

        // Assert
        assertThat(riskLevelBean.getPatId()).isEqualTo(patId);
    }

    @Test
    void testSetAndGetRiskLevel() {
        // Arrange
        RiskLevelBean riskLevelBean = new RiskLevelBean();
        String riskLevel = "Low";

        // Act
        riskLevelBean.setRiskLevel(riskLevel);

        // Assert
        assertThat(riskLevelBean.getRiskLevel()).isEqualTo(riskLevel);
    }

    @Test
    void testToString() {
        // Arrange
        Long patId = 789L;
        String riskLevel = "Medium";
        RiskLevelBean riskLevelBean = new RiskLevelBean(riskLevel, patId);

        // Act
        String toStringResult = riskLevelBean.toString();

        // Assert
        assertThat(toStringResult)
                .contains("RiskLevelBean{")
                .contains("patId=" + patId)
                .contains("riskLevel='" + riskLevel + "'");
    }
}
