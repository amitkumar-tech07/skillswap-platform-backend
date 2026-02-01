package com.backend.skillswap.repository;

import com.backend.skillswap.entity.SkillRequest;
import com.backend.skillswap.entity.UserEntity;
import com.backend.skillswap.entity.UserSkill;
import com.backend.skillswap.entity.enums.SkillRequestStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRequestRepository extends JpaRepository<SkillRequest, Long> {

    // Requests Sent requests by user
    List<SkillRequest> findBySender(UserEntity sender);

    // Requests Received requests for user
    List<SkillRequest> findByReceiver(UserEntity receiver);

    // Check Duplicates : same sender → receiver → skill → status not COMPLETED (status != COMPLETED)
    boolean existsBySenderAndReceiverAndSkillAndStatusIn(
            UserEntity sender,
            UserEntity receiver,
            UserSkill skill,
            List<SkillRequestStatus> statuses
    );

    // Skill Delete Check
    boolean existsBySkillAndStatusIn(
            UserSkill skill,
            List<SkillRequestStatus> statuses
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select sr from SkillRequest sr where sr.id = :id")
    Optional<SkillRequest> findByIdForUpdate(@Param("id") Long id);

    Optional<SkillRequest> findByIdAndStatus(Long id, SkillRequestStatus status);

    boolean existsByIdAndStatus(Long id, SkillRequestStatus status);

    // Find expired requests
    List<SkillRequest> findByStatusAndExpiresAtBefore(SkillRequestStatus status, LocalDateTime time);
}
