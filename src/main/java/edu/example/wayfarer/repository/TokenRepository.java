package edu.example.wayfarer.repository;

import edu.example.wayfarer.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByRefreshToken(String refreshToken);
    Optional<Token> findByMember_Email(String email);
    Optional<Token> findBySocialAccessToken(String socialAccessToken);
    void deleteByMember_Email(String email);
}