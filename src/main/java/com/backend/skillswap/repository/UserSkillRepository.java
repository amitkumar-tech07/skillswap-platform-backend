package com.backend.skillswap.repository;

import com.backend.skillswap.entity.UserEntity;
import com.backend.skillswap.entity.UserSkill;
import com.backend.skillswap.entity.enums.SkillCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {

    // Logged-in user ki ALL skills (active only)
    List<UserSkill> findByUserAndActiveTrue(UserEntity user);

    // Logged-in user ki skill (for update/delete ownership check)
    List<UserSkill> findByUserIdAndActiveTrue(Long userId);

    // Search skills by title (case-insensitive, public only)
    List<UserSkill> findByTitleContainingIgnoreCaseAndVerifiedTrueAndActiveTrue(String title);

    // Publicly visible skills (verified + active)
    List<UserSkill> findByVerifiedTrueAndActiveTrue();

    // Publicly visible skills of a specific user
    List<UserSkill> findByUserIdAndVerifiedTrueAndActiveTrue(Long userId);

    // Filter public skills by category
    List<UserSkill> findByCategoryAndVerifiedTrueAndActiveTrue(SkillCategory category);

    // Admin: fetch ALL skills of a user (active + inactive)
    List<UserSkill> findByUserId(Long userId);

    // Admin: fetch all unverified skills  (Pending skills only)
    List<UserSkill> findByVerifiedFalseAndActiveTrue();

    // Duplicate Skill Check
    boolean existsByUserIdAndTitleIgnoreCaseAndActiveTrue(Long userId, String title);

}
