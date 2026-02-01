package com.backend.skillswap.repository;

import com.backend.skillswap.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUser_Id(Long userId);

    boolean existsByUser_Id(Long userId);

}

