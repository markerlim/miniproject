package com.project.mini_ssf.model;

import java.time.LocalDate;

public class EntityDetails {

    private String uen;
    private String issuanceAgencyId;
    private String uenStatus;
    private String entityName;
    private String entityType;
    private LocalDate uenIssueDate;
    private String regStreetName;
    private String regPostalCode;

    public EntityDetails(){

    }
    
    public String getUen() {
        return uen;
    }

    public void setUen(String uen) {
        this.uen = uen;
    }

    public String getIssuanceAgencyId() {
        return issuanceAgencyId;
    }

    public void setIssuanceAgencyId(String issuanceAgencyId) {
        this.issuanceAgencyId = issuanceAgencyId;
    }

    public String getUenStatus() {
        return uenStatus;
    }

    public void setUenStatus(String uenStatus) {
        this.uenStatus = uenStatus;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public LocalDate getUenIssueDate() {
        return uenIssueDate;
    }

    public void setUenIssueDate(LocalDate uenIssueDate) {
        this.uenIssueDate = uenIssueDate;
    }

    public String getRegStreetName() {
        return regStreetName;
    }

    public void setRegStreetName(String regStreetName) {
        this.regStreetName = regStreetName;
    }

    public String getRegPostalCode() {
        return regPostalCode;
    }

    public void setRegPostalCode(String regPostalCode) {
        this.regPostalCode = regPostalCode;
    }
    
}
