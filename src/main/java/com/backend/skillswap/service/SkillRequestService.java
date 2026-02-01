package com.backend.skillswap.service;

import com.backend.skillswap.dto.request.SkillRequestRequest;
import com.backend.skillswap.dto.response.SkillRequestResponse;

import java.util.List;

public interface SkillRequestService {

    // Send a new request
    SkillRequestResponse sendRequest(Long senderId, SkillRequestRequest request);

    // Get sent requests
    List<SkillRequestResponse> mySentRequests(Long senderId);

    // Get received requests
    List<SkillRequestResponse> myReceivedRequests(Long receiverId);

    // Accept a request
    SkillRequestResponse acceptRequest(Long receiverId, Long requestId);

    // Reject a request
    SkillRequestResponse rejectRequest(Long receiverId, Long requestId);

    // sender can cancel a pending request before it’s accepted/rejected.
    SkillRequestResponse cancelRequest(Long senderId, Long requestId);

    // Mark complete
    SkillRequestResponse markCompleted(Long userId, Long requestId);

    // Auto-expire pending requests
    void autoExpireRequests();
}
