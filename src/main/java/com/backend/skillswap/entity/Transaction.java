package com.backend.skillswap.entity;

import com.backend.skillswap.entity.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "transactions",   // Represents a financial transaction for bookings, wallet, refunds, and escrow flows
        indexes = {
                @Index(name = "idx_tx_booking", columnList = "booking_id"),
                @Index(name = "idx_tx_payer", columnList = "payer_id"),
                @Index(name = "idx_tx_payee", columnList = "payee_id"),
                @Index(name = "idx_tx_reference", columnList = "transaction_reference"),
                @Index(name = "idx_tx_status", columnList = "status"),
                @Index(name = "idx_tx_created_at", columnList = "created_at")
        }
)
public class Transaction {

    // Primary key for transaction record
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ---- Booking Relation ----
    // Link to Booking (Kis booking ke liye payment ho raha hai)
    @ManyToOne(fetch = FetchType.LAZY, optional = true)              // (Transaction → ManyToOne Booking)
    @JoinColumn(name = "booking_id", nullable = true)
    private Booking booking;

    // ---- Users ----
    // User who is paying the amount (learner / buyer)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payer_id", nullable = false)
    private UserEntity payer;

    // User who receives the amount (mentor / seller)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payee_id", nullable = false)
    private UserEntity payee;

    // ---- Amount & Currency ----
    // Total transaction amount charged to payer
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    // Currency used for the transaction (default: INR)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Currency currency = Currency.INR;    // INR, USD, EUR

    // Final amount credited to payee after platform fee deduction
    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal netAmount = BigDecimal.ZERO; // netAmount is credited to payee after platform fee; zero for wallet-only transactions

    // Platform fee on this transaction (commission) e.g., 10%
    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal platformFee = BigDecimal.ZERO;   // Transaction Amount NULL protection

    // ---- Transaction Type & Status ----
    // Defines the business nature of transaction (DEPOSIT, REFUND, ESCROW, RELEASE)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    // Execution status of transaction (PENDING, SUCCESS, FAILED)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    // ---- Gateway Details ----
    // Unique payment gateway reference ID for reconciliation
    @Column(name = "transaction_reference", unique = true, nullable = false)
    private String transactionReference;   // Payment gateway ref. like Razorpay / Stripe ID

    // External or internal system used to process the payment like Razorpay / Stripe / PayPal / Internal
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentGateway paymentGateway;

    // Mode of payment Method used by the payer like : UPI / CARD / NET_BANKING / WALLET
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    // Stores failure reason if transaction fails
    private String failureReason;

    // ---- Escrow Support ----
    // Indicates whether the amount is held in escrow
    @Column(nullable = false)
    @Builder.Default
    private boolean escrow = false;    // true = money on hold

    // Scheduled time when escrowed funds will be released
    private LocalDateTime escrowReleaseAt;

    // Number of retry attempts made for this transaction
    @Builder.Default
    private int retryCount = 0;

    // Prevents double processing (refund/release) under concurrent requests
    @Version
    private Long version;

    // Human-readable description for audit & tracking
    private String description;

    // ---- Audit Fields ----
    // Auto-managed timestamps for transaction creation and updates
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;  //  Auto set when booking created

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;  //  Auto update when booking updated
}
