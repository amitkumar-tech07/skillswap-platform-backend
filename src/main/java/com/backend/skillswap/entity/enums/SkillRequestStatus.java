package com.backend.skillswap.entity.enums;

// Represents the lifecycle status of a SkillRequest (learner → provider)
public enum SkillRequestStatus {

    COMPLETED,   // Request fulfilled; session completed
    PENDING,     // Request sent; waiting for provider response
    ACCEPTED,    // Provider accepted the request
    BOOKED,      // Session officially booked (Booking entity created)
    REJECTED,    // Provider rejected the request
    CANCELLED,   // Sender cancelled before provider action
    EXPIRED      // Automatically expired after expiryTime
}
