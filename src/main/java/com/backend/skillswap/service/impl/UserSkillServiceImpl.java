package com.backend.skillswap.service.impl;

import com.backend.skillswap.dto.request.UserSkillRequest;
import com.backend.skillswap.dto.response.UserSkillResponse;
import com.backend.skillswap.entity.UserEntity;
import com.backend.skillswap.entity.UserSkill;
import com.backend.skillswap.entity.enums.SkillCategory;
import com.backend.skillswap.exception.common.DuplicateResourceException;
import com.backend.skillswap.exception.common.ResourceNotFoundException;
import com.backend.skillswap.exception.userSkill.SkillDeletionNotAllowedException;
import com.backend.skillswap.mapper.UserSkillMapper;
import com.backend.skillswap.repository.SkillRequestRepository;
import com.backend.skillswap.repository.UserRepository;
import com.backend.skillswap.repository.UserSkillRepository;
import com.backend.skillswap.service.EmailService;
import com.backend.skillswap.service.UserSkillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.backend.skillswap.entity.enums.SkillRequestStatus.ACCEPTED;
import static com.backend.skillswap.entity.enums.SkillRequestStatus.PENDING;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserSkillServiceImpl implements UserSkillService {

    private final UserSkillRepository userSkillRepository;
    private final UserRepository userRepository;
    private final SkillRequestRepository skillRequestRepository;
    private final EmailService emailService;

    // ================= USER: ADD SKILL =================
    @Override
    public UserSkillResponse addSkill(Long userId, UserSkillRequest request) {

        boolean exists = userSkillRepository
                .existsByUserIdAndTitleIgnoreCaseAndActiveTrue(userId, request.getTitle());

        if (exists) {
            throw new DuplicateResourceException("You already added this skill");
        }

        // Check User by userId
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserSkill skill = UserSkillMapper.toEntity(request, user);
        skill.setVerified(false); // new skill always unverified

        UserSkill saved = userSkillRepository.save(skill);
        return UserSkillMapper.toResponse(saved);
    }

    // ================= USER: UPDATE SKILL =================
    @Override
    public UserSkillResponse updateSkill(Long userId, Long skillId, UserSkillRequest request) {

        UserSkill skill = userSkillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));

        // Ownership check
        if (!skill.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("You are not allowed to update this skill");
        }

        boolean exists = userSkillRepository
                .existsByUserIdAndTitleIgnoreCaseAndActiveTrue(userId, request.getTitle());

        if (exists && !skill.getTitle().equalsIgnoreCase(request.getTitle())) {
            throw new DuplicateResourceException("Skill already exists");
        }

        // Update entity
        UserSkillMapper.updateEntity(skill, request);
        skill.setVerified(false);  // must re-verify after update

        UserSkill updated = userSkillRepository.save(skill);
        return UserSkillMapper.toResponse(updated);
    }


    // ================= USER: DELETE SKILL (SOFT) =================
    @Override
    public void deleteSkill(Long userId, Long skillId) {

        UserSkill skill = userSkillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));


        // Ownership check
        if (!skill.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("You are not allowed to delete this skill");
        }

        // Already deleted check
        if (!skill.isActive()) {
            throw new ResourceNotFoundException("Skill is already deleted");
        }

        // Active request check
        boolean hasActiveRequests = skillRequestRepository.existsBySkillAndStatusIn(skill, List.of(PENDING, ACCEPTED));

        if (hasActiveRequests) {
            throw new SkillDeletionNotAllowedException("This skill has active or accepted bookings and cannot be deleted.");
        }

        // Soft delete
        skill.setActive(false);   // delete
        skill.setVerified(false);  // Skill - Unverify
        userSkillRepository.save(skill);

        // Send success email to user
        emailService.sendSkillDeletedMail(skill);  // Sends confirmation email when user deletes their own skill

    }


    // ================= USER: RESTORE SKILL =================
    @Override
    public void restoreSkill(Long userId, Long skillId) {

        UserSkill skill = userSkillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));

        // Ownership check
        if (!skill.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("You are not allowed to restore this skill");
        }

        // Already active
        if (skill.isActive()) {
            throw new ResourceNotFoundException("Skill is already active");
        }

        // Restore + Auto-unverify
        skill.setActive(true);
        skill.setVerified(false); //  must re-verify
        userSkillRepository.save(skill);

        // Extract everything BEFORE async
        String email = skill.getUser().getEmail();
        String fullName = skill.getUser().getUserProfile().getFullName();
        String title = skill.getTitle();
        SkillCategory category = skill.getCategory();

        // Async mail (SAFE) Email notification
        emailService.sendSkillRestoredMail(email, fullName, title, category, skill.getId());
    }

    // ================= USER: MY SKILLS =================
    @Override
    @Transactional(readOnly = true)
    public List<UserSkillResponse> getMySkills(Long userId) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return userSkillRepository.findByUserAndActiveTrue(user)
                .stream()
                .map(UserSkillMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ================= PUBLIC: USER SKILLS =================
    @Override
    @Transactional(readOnly = true)
    public List<UserSkillResponse> getUserSkills(Long userId) {

        return userSkillRepository.findByUserIdAndVerifiedTrueAndActiveTrue(userId)
                .stream()
                .map(UserSkillMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ================= PUBLIC: SEARCH =================
    @Override
    @Transactional(readOnly = true)
    public List<UserSkillResponse> searchSkills(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllVerifiedSkills();
        }

        return userSkillRepository
                .findByTitleContainingIgnoreCaseAndVerifiedTrueAndActiveTrue(keyword)
                .stream()
                .map(UserSkillMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ================= PUBLIC: ALL VERIFIED =================
    @Override
    @Transactional(readOnly = true)
    public List<UserSkillResponse> getAllVerifiedSkills() {

        return userSkillRepository.findByVerifiedTrueAndActiveTrue()
                .stream()
                .map(UserSkillMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ================= ADMIN: VERIFY SKILL =================
    @Override
    public void verifySkill(Long skillId) {

        UserSkill skill = userSkillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));

        // Already verified
        if (skill.isVerified()) {
            throw new ResourceNotFoundException("Skill is already verified");
        }

        // Cannot verify inactive skill
        if (!skill.isActive()) {
            throw new IllegalStateException("Cannot verify inactive skill");
        }

        skill.setVerified(true);
        skill.setActive(true);
        userSkillRepository.save(skill);

        // SEND EMAIL
        emailService.sendSkillVerifiedMail(skill);
    }


    // ================= ADMIN: REJECT SKILL =================
    @Override
    public void rejectSkill(Long skillId) {

        UserSkill skill = userSkillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));

        // Already rejected (inactive + not verified)
        if (!skill.isActive() && !skill.isVerified()) {
            throw new ResourceNotFoundException("Skill is already rejected");
        }

        skill.setVerified(false);
        skill.setActive(false);
        userSkillRepository.save(skill);

        // SEND EMAIL
        emailService.sendSkillRejectedMail(skill);
    }


    @Override
    @Transactional(readOnly = true)
    public List<UserSkillResponse> getMySkillsByUserId(Long userId) {

        return userSkillRepository.findByUserIdAndActiveTrue(userId)
                .stream()
                .map(UserSkillMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSkillResponse> getSkillsByCategory(SkillCategory category) {

        return userSkillRepository
                .findByCategoryAndVerifiedTrueAndActiveTrue(category)
                .stream()
                .map(UserSkillMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSkillResponse> getAllSkillsOfUser(Long userId) {

        return userSkillRepository.findByUserId(userId)
                .stream()
                .map(UserSkillMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSkillResponse> getUnverifiedSkills() {

        return userSkillRepository.findByVerifiedFalseAndActiveTrue()
                .stream()
                .map(UserSkillMapper::toResponse)
                .collect(Collectors.toList());
    }
}

