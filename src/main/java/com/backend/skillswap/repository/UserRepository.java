package com.backend.skillswap.repository;

import com.backend.skillswap.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository  extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsernameIgnoreCase(String username);

    Optional<UserEntity> findByEmailIgnoreCase(String email);  // Case-insensitive email search

    // Hybrid login support (email or username case-insensitive)
    Optional<UserEntity> findByEmailIgnoreCaseOrUsernameIgnoreCase(String email, String username); // Hybrid method

}

