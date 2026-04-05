package com.grimni.domain;

import java.util.Date;

import jakarta.persistence.*;

@Entity(name="RefreshToken")
@Table(name="refreshTokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tokenId;

    // FK relation between refreshTokens and userIds
    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

    private String tokenValue;
    private Date createdAt;
    

    public RefreshToken() {} // no-args constructor

    public Long getTokenId() {
        return tokenId;
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public User getUser() {
        return user;
    }

    public Date getCreationDate() {
        return createdAt;
    }

    public void setTokenId(Long tokenId) {
        this.tokenId = tokenId;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
