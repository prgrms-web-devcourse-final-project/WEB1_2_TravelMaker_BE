package edu.example.wayfarer.repository;

import edu.example.wayfarer.entity.Token;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends CrudRepository<Token, String> {
    Optional<Token> findByRefreshToken(String refreshToken);
    Optional<Token> findByEmail(String email);
    void deleteByEmail(String email);
}