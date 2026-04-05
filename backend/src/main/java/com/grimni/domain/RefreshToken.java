package com.grimni.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "refresh_token")
public class RefreshToken extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "org_id")
    private Long orgId;

    public RefreshToken() {} // no-args constructor

    public Long getId() {
        return this.id;
    }
    public String getRefreshToken() {
        return this.refreshToken;
    }
    public User getUser() {
        return this.user;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

}