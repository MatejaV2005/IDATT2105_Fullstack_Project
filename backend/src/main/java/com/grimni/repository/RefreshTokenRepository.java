package com.grimni.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.grimni.domain.RefreshToken;
import com.grimni.domain.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long>{
    
    Optional<RefreshToken> findByTokenValue(String tokenValue);

    List<RefreshToken> findByUser(User user);

    void deleteByUser(User user);
    
}
