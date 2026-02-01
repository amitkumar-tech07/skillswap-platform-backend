package com.backend.skillswap.service;

import com.backend.skillswap.entity.Booking;
import com.backend.skillswap.entity.UserSkill;
import com.backend.skillswap.entity.enums.SkillCategory;

import java.util.concurrent.CompletableFuture;

public interface EmailService {

    // ===================== User OTP / Verification =====================
    CompletableFuture<Boolean> sendVerificationOtpEmail(String toEmail, String otp);

    void sendPasswordResetOtpEmail(String toEmail, String otp);

    void sendPasswordResetConfirmation(String toEmail);

    // ==================== User Skill verification email ================
    void sendSkillVerifiedMail(UserSkill skill);

    void sendSkillRejectedMail(UserSkill skill);

    void sendSkillDeletedMail(UserSkill skill);

    // Skill restore
    void sendSkillRestoredMail(String email,String fullName, String skillTitle, SkillCategory category, Long skillId);

    // ===================== Booking Notifications =====================
    // Notify user that a booking has been created successfully; professional transactional email
    void sendBookingCreatedMail(Booking booking);

    // Notify user that their booking has been confirmed; professional transactional email
    void sendBookingConfirmedMail(Booking booking);

    // Notify user that their booking has been cancelled; includes reason if available
    void sendBookingCancelledMail(Booking booking);

    // Notify provider about a new booking request received; transactional & informative
    void sendProviderBookingCreatedMail(Booking booking);

    // Notify provider that booking was successfully confirmed by them
    void sendProviderBookingConfirmedMail(Booking booking);

    // Notify provider that a booking was cancelled; includes reason
    void sendProviderBookingCancelledMail(Booking booking);

    // Notify requester that the session has started; professional notification
    void sendBookingStartedMail(Booking booking);

    // Notify requester that the session has completed successfully
    void sendBookingCompletedMail(Booking booking);

    // Notify requester that a dispute has been raised for their booking
    void sendBookingDisputedMail(Booking booking);

    // ===================== Transactional Emails =====================
    // Sends email for transaction events asynchronously; e.g., deposit, withdraw, escrow release, refund
    void sendTransactionMail(String to, String subject, String body);

}
