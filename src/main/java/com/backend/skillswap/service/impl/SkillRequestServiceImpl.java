package com.backend.skillswap.service.impl;

import com.backend.skillswap.dto.request.SkillRequestRequest;
import com.backend.skillswap.dto.response.SkillRequestResponse;
import com.backend.skillswap.entity.*;
import com.backend.skillswap.entity.enums.SkillRequestStatus;
import com.backend.skillswap.exception.common.ResourceNotFoundException;
import com.backend.skillswap.mapper.SkillRequestMapper;
import com.backend.skillswap.repository.*;
import com.backend.skillswap.service.SkillRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SkillRequestServiceImpl implements SkillRequestService {

    private final SkillRequestRepository skillRequestRepository;
    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;

    // ===================== SEND REQUEST =====================
    @Override
    public SkillRequestResponse sendRequest(Long senderId, SkillRequestRequest requestDto) {

        UserEntity sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

        UserSkill skill = userSkillRepository.findById(requestDto.getSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));

        UserEntity receiver = skill.getUser();

        //  VALIDATION: Self request
        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalStateException("You cannot send request to yourself");
        }

        //  VALIDATION: Duplicate request
        boolean exists = skillRequestRepository
                .existsBySenderAndReceiverAndSkillAndStatusIn(
                        sender,
                        receiver,
                        skill,
                        List.of(SkillRequestStatus.PENDING, SkillRequestStatus.ACCEPTED)
                );

        if (exists) {
            throw new IllegalStateException("Active request already exists for this skill");
        }

        SkillRequest request = SkillRequestMapper.toEntity(requestDto, sender, receiver, skill);

        // Set Expiry time here
        request.setExpiresAt(LocalDateTime.now().plusHours(48)); // or plusDays(2)

        SkillRequest saved = skillRequestRepository.save(request);

        return SkillRequestMapper.toResponse(saved);
    }

    // ===================== SENT REQUESTS =====================
    @Override
    @Transactional(readOnly = true)
    public List<SkillRequestResponse> mySentRequests(Long senderId) {
        UserEntity sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return skillRequestRepository.findBySender(sender)
                .stream()
                .map(SkillRequestMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ===================== RECEIVED REQUESTS =====================
    @Override
    @Transactional(readOnly = true)
    public List<SkillRequestResponse> myReceivedRequests(Long receiverId) {
        UserEntity receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return skillRequestRepository.findByReceiver(receiver)
                .stream()
                .map(SkillRequestMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ===================== ACCEPT REQUEST =====================
    @Override
    public SkillRequestResponse acceptRequest(Long receiverId, Long requestId) {

        SkillRequest request = skillRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        ensureNotTerminal(request);

        //  AUTH CHECK
        if (!request.getReceiver().getId().equals(receiverId)) {
            throw new IllegalStateException("You are not allowed to accept this request");
        }

        // STATUS CHECK
        if (request.getStatus() != SkillRequestStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be accepted");
        }

        request.setStatus(SkillRequestStatus.ACCEPTED);
        SkillRequest saved = skillRequestRepository.save(request);

        return SkillRequestMapper.toResponse(saved);
    }

    // ===================== REJECT REQUEST =====================
    @Override
    public SkillRequestResponse rejectRequest(Long receiverId, Long requestId) {

        SkillRequest request = skillRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        ensureNotTerminal(request);

        //  AUTH CHECK
        if (!request.getReceiver().getId().equals(receiverId)) {
            throw new IllegalStateException("You are not allowed to reject this request");
        }

        //  STATUS CHECK
        if (request.getStatus() != SkillRequestStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be rejected");
        }

        request.setStatus(SkillRequestStatus.REJECTED);

        return SkillRequestMapper.toResponse(skillRequestRepository.save(request));
    }

    // ===================== CANCEL REQUEST =====================
    @Override
    public SkillRequestResponse cancelRequest(Long senderId, Long requestId) {

        // Fetch SkillRequest first
        SkillRequest request = skillRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        // Block cancellation if already BOOKED
        if (request.getStatus() == SkillRequestStatus.BOOKED) {
            throw new ResourceNotFoundException("Cannot cancel request after booking is created");
        }

        // Terminal state protection (COMPLETED / REJECTED / EXPIRED etc.)
        ensureNotTerminal(request);

        // AUTH CHECK → only sender can cancel
        if (!request.getSender().getId().equals(senderId)) {
            throw new IllegalStateException("You are not allowed to cancel this request");
        }

        // STATUS CHECK → only PENDING allowed
        if (request.getStatus() != SkillRequestStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be cancelled");
        }

        // Cancel
        request.setStatus(SkillRequestStatus.CANCELLED);

        return SkillRequestMapper.toResponse(skillRequestRepository.save(request));
    }

    // ===================== MARK COMPLETED =====================
    @Override
    public SkillRequestResponse markCompleted(Long userId, Long requestId) {

        SkillRequest request = skillRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        ensureNotTerminal(request);

        // AUTH CHECK
        if (!request.getSender().getId().equals(userId) &&
                !request.getReceiver().getId().equals(userId)) {
            throw new IllegalStateException("Not authorized to mark completed");
        }

        //  STATUS CHECK
        if (request.getStatus() != SkillRequestStatus.ACCEPTED) {
            throw new IllegalStateException("Only accepted requests can be completed");
        }

        request.setStatus(SkillRequestStatus.COMPLETED);

        return SkillRequestMapper.toResponse(skillRequestRepository.save(request));
    }

    // ===================== AUTO EXPIRE =====================
    @Override
    @Scheduled(cron = "0 0 * * * *")  // Runs every hour
    public void autoExpireRequests() {

        List<SkillRequest> expired = skillRequestRepository
                .findByStatusAndExpiresAtBefore(SkillRequestStatus.PENDING, LocalDateTime.now());

        expired.forEach(req -> {
            // Extra safety
            if (req.getStatus() == SkillRequestStatus.PENDING) {
                req.setStatus(SkillRequestStatus.EXPIRED);
            }
        });

        skillRequestRepository.saveAll(expired);
    }

    // ===================== HELPER: TERMINAL CHECK =====================
    private void ensureNotTerminal(SkillRequest request) {
        if (request.getStatus() == SkillRequestStatus.COMPLETED ||
                request.getStatus() == SkillRequestStatus.REJECTED ||
                request.getStatus() == SkillRequestStatus.CANCELLED ||
                request.getStatus() == SkillRequestStatus.EXPIRED) {
            throw new IllegalStateException("Request already finalized");
        }
    }
}
