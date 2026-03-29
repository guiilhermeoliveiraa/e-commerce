package com.javacore.spring_api_app.repository.token;

import com.javacore.spring_api_app.entity.token.RefreshToken;
import com.javacore.spring_api_app.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenId(String tokenId);
    List<RefreshToken> findAllByUser(User user);
}
