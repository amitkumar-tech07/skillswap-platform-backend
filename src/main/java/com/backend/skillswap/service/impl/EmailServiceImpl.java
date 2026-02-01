package com.backend.skillswap.service.impl;

import com.backend.skillswap.entity.Booking;
import com.backend.skillswap.entity.UserSkill;
import com.backend.skillswap.entity.enums.SkillCategory;
import com.backend.skillswap.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    // -------------------- SEND HTML EMAIL --------------------
    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML
            mailSender.send(message);
            log.info("Email sent to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}", to, e);
        }
    }

    // -------------------- EMAIL VERIFICATION OTP --------------------
    @Async
    @Override
    public CompletableFuture<Boolean> sendVerificationOtpEmail(String toEmail, String otp) {
        try {
            String html = "<h2>SkillSwap - Email Verification</h2>" +
                    "<p>Your OTP is: <b>" + otp + "</b></p>" +
                    "<p>This OTP is valid for 10 minutes.</p>" +
                    "<p style='color:red;'>Do not share it with anyone.</p>";
            sendHtmlEmail(toEmail, "SkillSwap - Email Verification", html);
            log.info("OTP email sent successfully to {}", toEmail);
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}", toEmail, e);
            return CompletableFuture.completedFuture(false);
        }
    }

    // -------------------- PASSWORD RESET OTP --------------------
    @Async
    @Override
    public void sendPasswordResetOtpEmail(String toEmail, String otp) {
        try {
            String html = "<h2>SkillSwap - Password Reset</h2>" +
                    "<p>Your password reset OTP is: <b>" + otp + "</b></p>" +
                    "<p>This OTP is valid for 10 minutes.</p>" +
                    "<p>If you didn't request, ignore this email.</p>";
            sendHtmlEmail(toEmail, "SkillSwap - Password Reset", html);
            log.info("Password reset OTP email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset OTP email to {}", toEmail, e);
        }
    }

    // -------------------- PASSWORD RESET CONFIRMATION --------------------
    @Async
    @Override
    public void sendPasswordResetConfirmation(String toEmail) {
        try {
            String html = "<h2>SkillSwap - Password Reset Successful</h2>" +
                    "<p>Your password has been successfully reset.</p>" +
                    "<p>If you didn't perform this action, contact support immediately.</p>";
            sendHtmlEmail(toEmail, "Password Reset Successful", html);
            log.info("Password reset confirmation email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset confirmation email to {}", toEmail, e);
        }
    }

    // ==================== User Skill verification email ================
    @Override
    public void sendSkillVerifiedMail(UserSkill skill) {

        String html = """
        <html>
        <body style="margin:0;padding:0;background:#f4f6f8;font-family:Arial,sans-serif;">
            <div style="max-width:600px;margin:30px auto;background:#ffffff;border-radius:8px;overflow:hidden;">
                
                <div style="background:#22c55e;color:white;padding:20px;text-align:center;">
                    <h1 style="margin:0;">🎉 Skill Approved</h1>
                </div>

                <div style="padding:30px;color:#333;">
                    <p>Hello <b>%s</b>,</p>

                    <p>Great news! Your skill has been <b>successfully verified</b> by our admin team.</p>

                    <div style="background:#f0fdf4;padding:15px;border-radius:6px;margin:20px 0;">
                        <p><b>Skill:</b> %s</p>
                        <p><b>Category:</b> %s</p>
                        <p><b>Level:</b> %s</p>
                        <p><b>Hourly Rate:</b> ₹%s</p>
                    </div>

                    <p>Your skill is now <b>live on SkillSwap</b> and users can start booking sessions with you.</p>

                    <a href="https://skillswap.com/dashboard"
                       style="display:inline-block;margin-top:20px;padding:12px 20px;
                              background:#22c55e;color:white;text-decoration:none;
                              border-radius:6px;font-weight:bold;">
                        Go to Dashboard
                    </a>
                </div>

                <div style="background:#f9fafb;text-align:center;padding:15px;font-size:12px;color:#6b7280;">
                    © 2026 SkillSwap. All rights reserved.
                </div>
            </div>
        </body>
        </html>
        """.formatted(
                skill.getUser().getUserProfile().getFullName(),
                skill.getTitle(),
                skill.getCategory(),
                skill.getLevel(),
                skill.getHourlyRate()
        );

        sendHtmlEmail(
                skill.getUser().getEmail(),
                "🎉 Your Skill Has Been Approved - SkillSwap",
                html
        );
    }

    @Override
    public void sendSkillRejectedMail(UserSkill skill) {

        String html = """
        <html>
        <body style="margin:0;padding:0;background:#f4f6f8;font-family:Arial,sans-serif;">
            <div style="max-width:600px;margin:30px auto;background:#ffffff;border-radius:8px;">
                
                <div style="background:#ef4444;color:white;padding:20px;text-align:center;">
                    <h1 style="margin:0;">❌ Skill Rejected</h1>
                </div>

                <div style="padding:30px;color:#333;">
                    <p>Hello <b>%s</b>,</p>

                    <p>Your skill <b>%s</b> could not be approved at this time.</p>

                    <p>Please review your skill details and update them for re-verification.</p>

                    <p style="color:#6b7280;">
                        If you believe this is a mistake, feel free to contact our support team.
                    </p>

                    <a href="https://skillswap.com/dashboard"
                       style="display:inline-block;margin-top:20px;padding:12px 20px;
                              background:#ef4444;color:white;text-decoration:none;
                              border-radius:6px;font-weight:bold;">
                        Update Skill
                    </a>
                </div>

                <div style="background:#f9fafb;text-align:center;padding:15px;font-size:12px;color:#6b7280;">
                    © 2026 SkillSwap. All rights reserved.
                </div>
            </div>
        </body>
        </html>
        """.formatted(
                skill.getUser().getUserProfile().getFullName(),
                skill.getTitle()
        );

        sendHtmlEmail(
                skill.getUser().getEmail(),
                "❌ Skill Rejected - SkillSwap",
                html
        );
    }

    @Override
    public void sendSkillDeletedMail(UserSkill skill) {

        String html = """
        <html>
        <body style="margin:0;padding:0;background:#f4f6f8;font-family:Arial,sans-serif;">
            <div style="max-width:600px;margin:30px auto;background:#ffffff;border-radius:8px;overflow:hidden;">
                
                <div style="background:#0f172a;color:white;padding:20px;text-align:center;">
                    <h1 style="margin:0;">🗑️ Skill Deleted</h1>
                </div>

                <div style="padding:30px;color:#333;">
                    <p>Hello <b>%s</b>,</p>

                    <p>Your skill has been <b>successfully deleted</b> from SkillSwap.</p>

                    <div style="background:#f1f5f9;padding:15px;border-radius:6px;margin:20px 0;">
                        <p><b>Skill Name:</b> %s</p>
                        <p><b>Category:</b> %s</p>
                        <p><b>Level:</b> %s</p>
                    </div>

                    <p>
                        This skill is no longer visible to users and cannot receive new bookings.
                    </p>

                    <p style="color:#6b7280;">
                        You can add a new skill anytime from your dashboard.
                    </p>

                    <a href="https://skillswap.com/dashboard"
                       style="display:inline-block;margin-top:20px;padding:12px 20px;
                              background:#0f172a;color:white;text-decoration:none;
                              border-radius:6px;font-weight:bold;">
                        Go to Dashboard
                    </a>
                </div>

                <div style="background:#f9fafb;text-align:center;padding:15px;font-size:12px;color:#6b7280;">
                    © 2026 SkillSwap. All rights reserved.
                </div>
            </div>
        </body>
        </html>
        """.formatted(
                skill.getUser().getUserProfile().getFullName(),
                skill.getTitle(),
                skill.getCategory(),
                skill.getLevel()
        );

        sendHtmlEmail(
                skill.getUser().getEmail(),
                "🗑️ Skill Deleted Successfully - SkillSwap",
                html
        );
    }

    // -------------------- SKILL RESTORED EMAIL --------------------
    @Async
    @Override
    public void sendSkillRestoredMail(String email, String fullName, String skillTitle, SkillCategory category, Long skillId) {
        try {
            String html =
                    "<!DOCTYPE html>" +
                            "<html><body>" +
                            "<h2>Skill Restored Successfully</h2>" +
                            "<p>Hello <b>" + fullName + "</b>,</p>" +
                            "<p>Your skill has been restored and is visible again.</p>" +
                            "<p><b>Skill:</b> " + skillTitle + "<br/>" +
                            "<b>Category:</b> " + category + "</p>" +
                            "<p>⚠️ This skill requires admin verification again.</p>" +
                            "<p>Thank you for using <b>SkillSwap</b>.</p>" +
                            "</body></html>";

            sendHtmlEmail(
                    email,
                    "Skill Restored – Verification Required",
                    html
            );

            log.info("Skill restored email sent for skillId={}", skillId);

        } catch (Exception e) {
            log.error("Failed to send skill restored email", e);
        }
    }

    // -------------------- BOOKING EMAILS --------------------
    @Async
    @Override
    public void sendBookingCreatedMail(Booking booking) {
        try {
            String html = "<h2>Booking Created Successfully!</h2>" +
                    "<p>Hello <b>" + booking.getRequester().getUserProfile().getFullName() + "</b>,</p>" +
                    "<p>Your booking for skill '<b>" + booking.getSkill().getTitle() + "</b>' has been created.</p>" +
                    "<p>Start Time: " + booking.getStartTime() + "</p>" +
                    "<p>Provider: " + booking.getProvider().getUserProfile().getFullName() + "</p>";
            sendHtmlEmail(booking.getRequester().getEmail(), "Booking Created Successfully", html);
        } catch (Exception e) {
            log.error("Failed to send booking created email", e);
        }
    }

    @Async
    @Override
    public void sendProviderBookingCreatedMail(Booking booking) {
        try {
            String html = "<h2>New Booking Request Received!</h2>" +
                    "<p>Hello <b>" + booking.getProvider().getUserProfile().getFullName() + "</b>,</p>" +
                    "<p>You have received a booking request for skill '<b>" + booking.getSkill().getTitle() + "</b>'.</p>" +
                    "<p>Start Time: " + booking.getStartTime() + "</p>" +
                    "<p>Requester: " + booking.getRequester().getUserProfile().getFullName() + "</p>";
            sendHtmlEmail(booking.getProvider().getEmail(), "New Booking Request Received", html);
        } catch (Exception e) {
            log.error("Failed to send provider booking created email", e);
        }
    }

    @Async
    @Override
    public void sendBookingConfirmedMail(Booking booking) {
        try {
            String html = "<h2>Booking Confirmed</h2>" +
                    "<p>Hello <b>" + booking.getRequester().getUserProfile().getFullName() + "</b>,</p>" +
                    "<p>Your booking for skill '<b>" + booking.getSkill().getTitle() + "</b>' has been confirmed.</p>" +
                    "<p>Start Time: " + booking.getStartTime() + "</p>" +
                    "<p>Provider: " + booking.getProvider().getUserProfile().getFullName() + "</p>";
            sendHtmlEmail(booking.getRequester().getEmail(), "Booking Confirmed", html);
        } catch (Exception e) {
            log.error("Failed to send booking confirmed email", e);
        }
    }

    @Async
    @Override
    public void sendProviderBookingConfirmedMail(Booking booking) {
        try {
            String html = "<h2>Booking Confirmed by You</h2>" +
                    "<p>Hello <b>" + booking.getProvider().getUserProfile().getFullName() + "</b>,</p>" +
                    "<p>You confirmed a booking for skill '<b>" + booking.getSkill().getTitle() + "</b>'.</p>" +
                    "<p>Start Time: " + booking.getStartTime() + "</p>" +
                    "<p>Requester: " + booking.getRequester().getUserProfile().getFullName() + "</p>";
            sendHtmlEmail(booking.getProvider().getEmail(), "Booking Confirmed by You", html);
        } catch (Exception e) {
            log.error("Failed to send provider booking confirmed email", e);
        }
    }

    @Async
    @Override
    public void sendBookingCancelledMail(Booking booking) {
        try {
            String reason = booking.getCancelReason() != null ? booking.getCancelReason() : "No reason provided";
            String html = "<h2>Booking Cancelled</h2>" +
                    "<p>Hello <b>" + booking.getRequester().getUserProfile().getFullName() + "</b>,</p>" +
                    "<p>Your booking for skill '<b>" + booking.getSkill().getTitle() + "</b>' has been cancelled.</p>" +
                    "<p>Reason: " + reason + "</p>" +
                    "<p>Provider: " + booking.getProvider().getUserProfile().getFullName() + "</p>";
            sendHtmlEmail(booking.getRequester().getEmail(), "Booking Cancelled", html);
        } catch (Exception e) {
            log.error("Failed to send booking cancelled email", e);
        }
    }

    @Async
    @Override
    public void sendProviderBookingCancelledMail(Booking booking) {
        try {
            String reason = booking.getCancelReason() != null ? booking.getCancelReason() : "No reason provided";
            String html = "<h2>Booking Cancelled</h2>" +
                    "<p>Hello <b>" + booking.getProvider().getUserProfile().getFullName() + "</b>,</p>" +
                    "<p>A booking for skill '<b>" + booking.getSkill().getTitle() + "</b>' has been cancelled.</p>" +
                    "<p>Reason: " + reason + "</p>" +
                    "<p>Requester: " + booking.getRequester().getUserProfile().getFullName() + "</p>";
            sendHtmlEmail(booking.getProvider().getEmail(), "Booking Cancelled", html);
        } catch (Exception e) {
            log.error("Failed to send provider booking cancelled email", e);
        }
    }

    @Async
    @Override
    public void sendBookingStartedMail(Booking booking) {
        try {
            String html = "<h2>Booking Started</h2>" +
                    "<p>Hello <b>" + booking.getRequester().getUserProfile().getFullName() + "</b>,</p>" +
                    "<p>Your session for skill '<b>" + booking.getSkill().getTitle() + "</b>' has started.</p>" +
                    "<p>Provider: " + booking.getProvider().getUserProfile().getFullName() + "</p>";
            sendHtmlEmail(booking.getRequester().getEmail(), "Booking Started", html);
        } catch (Exception e) {
            log.error("Failed to send booking started email", e);
        }
    }

    @Async
    @Override
    public void sendBookingCompletedMail(Booking booking) {
        try {
            String html = "<h2>Booking Completed</h2>" +
                    "<p>Hello <b>" + booking.getRequester().getUserProfile().getFullName() + "</b>,</p>" +
                    "<p>Your session for skill '<b>" + booking.getSkill().getTitle() + "</b>' has been completed successfully.</p>" +
                    "<p>Provider: " + booking.getProvider().getUserProfile().getFullName() + "</p>" +
                    "<p>Please leave a review!</p>";
            sendHtmlEmail(booking.getRequester().getEmail(), "Booking Completed", html);
        } catch (Exception e) {
            log.error("Failed to send booking completed email", e);
        }
    }

    @Async
    @Override
    public void sendBookingDisputedMail(Booking booking) {
        try {
            String html = "<h2>Booking Dispute Raised</h2>" +
                    "<p>Hello <b>" + booking.getRequester().getUserProfile().getFullName() + "</b>,</p>" +
                    "<p>A dispute has been raised for your booking of skill '<b>" + booking.getSkill().getTitle() + "</b>'.</p>" +
                    "<p>Provider: " + booking.getProvider().getUserProfile().getFullName() + "</p>";
            sendHtmlEmail(booking.getRequester().getEmail(), "Booking Dispute Raised", html);
        } catch (Exception e) {
            log.error("Failed to send booking dispute email", e);
        }
    }

    // -------------------- TRANSACTION / REVIEW EMAILS --------------------
    @Async
    @Override
    public void sendTransactionMail(String to, String subject, String body) {
        try {
            sendHtmlEmail(to, subject, "<p>" + body + "</p>");
        } catch (Exception e) {
            log.error("Failed to send transaction email to {}", to, e);
        }
    }

}
