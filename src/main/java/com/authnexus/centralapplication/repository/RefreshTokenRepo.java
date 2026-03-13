package com.authnexus.centralapplication.repository;

import com.authnexus.centralapplication.domains.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByJti(String jti);

}
