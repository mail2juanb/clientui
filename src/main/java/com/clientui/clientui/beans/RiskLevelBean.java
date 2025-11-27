package com.clientui.clientui.beans;

public class RiskLevelBean {

    private Long patId;

    private String riskLevel;

    // Constructors
    public RiskLevelBean() {
    }

    public RiskLevelBean(String riskLevel, Long patId) {
        this.riskLevel = riskLevel;
        this.patId = patId;
    }


    // Getters Setters
    public String getRiskLevel() {
        return riskLevel;
    }

    // NOTE : Pas besoin de setter puisqu'on ne fait que lire les données, jamais les modifier
//    public void setRiskLevel(String riskLevel) {
//        this.riskLevel = riskLevel;
//    }

    public Long getPatId() {
        return patId;
    }

    // NOTE : Pas besoin de setter puisqu'on ne fait que lire les données, jamais les modifier
//    public void setPatId(Long patId) {
//        this.patId = patId;
//    }



    @Override
    public String toString() {
        return "RiskLevelBean{" +
                "patId=" + patId +
                ", riskLevel='" + riskLevel + '\'' +
                '}';
    }
}
