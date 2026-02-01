package com.backend.skillswap.repository;

import com.backend.skillswap.entity.EmailOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {

    Optional<EmailOtp> findByEmail(String email);

    void deleteByEmail(String email);

    boolean existsByEmail(String email);

}
