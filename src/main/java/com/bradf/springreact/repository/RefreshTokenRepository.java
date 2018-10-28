package com.bradf.springreact.repository;

import com.bradf.springreact.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByUserIdIs(Long id);
    Optional<RefreshToken> findByAccessTokenIs(String refreshToken);
}
