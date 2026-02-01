package com.backend.skillswap.dto.response;

import com.backend.skillswap.entity.enums.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.backend.skillswap.entity.enums.TransactionStatus.SUCCESS;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {

    private Long transactionId;
    private Long bookingId;

    // Users
    private Long payerId;
    private Long payeeId;

    // Amounts
    private BigDecimal amount;
    private BigDecimal platformFee;
    private BigDecimal netAmount;

    // Payment info
    private Currency currency;
    private TransactionType transactionType;
    private TransactionStatus status;

    // Escrow
    private boolean escrow;
    private LocalDateTime escrowReleaseAt;

    // Gateway info
    private String transactionReference;
    private PaymentGateway paymentGateway;
    private PaymentMethod paymentMethod;

    private String message;   // Clear API message

    private String failureReason;   // Failure handling

    public boolean isSuccess() {
        return status == SUCCESS;
    }

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
