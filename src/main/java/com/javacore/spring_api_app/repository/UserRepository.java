package com.javacore.spring_api_app.repository;

import com.javacore.spring_api_app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailAndDeletedFalse(String email);
    boolean existsByEmailAndDeletedFalse(String email);
}
