package com.grimni.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "organization")
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orgId;

    private String address;

    private String license;

    private String orgName;
    
    public Long getId() { return orgId; }
    public void setId(Long id) { this.orgId = id; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getLicense() { return license; }
    public void setLicense(String license) { this.license = license; }
    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }
}