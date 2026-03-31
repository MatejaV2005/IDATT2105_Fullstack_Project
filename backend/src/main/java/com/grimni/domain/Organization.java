package com.grimni.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "organizations")
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orgId;

    private String address;

    private String license;

    // @Column(unique = true)
    private String orgName;
    
    public Long getId() { return orgId; }
    public void setId(Long id) { this.orgId = id; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getLicense() { return license; }
    public void setLicense(String license) { this.license = license; }
    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }

    // @Override
    // public boolean equals(Object o) {
    //     if (this == o) return true;
    //     if (o == null || getClass() != o.getClass()) return false;
    //     Organization that = (Organization) o;
    //     return Objects.equals(id, that.id);
    // }
    // @Override
    // public int hashCode() {
    //     return Objects.hash(id);
    // }
    // @Override
    // public String toString() {
    //     return "Organization{id=" + id + ", orgName='" + orgName + "', license='" + license + "'}";
    // }
}