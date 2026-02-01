package com.backend.skillswap.mapper;

import com.backend.skillswap.dto.response.TransactionResponse;
import com.backend.skillswap.entity.Transaction;

public class TransactionMapper {

    private TransactionMapper() {
        // Utility class
    }

    // Entity -> Response DTO
    public static TransactionResponse toResponse(Transaction transaction) {

        return TransactionResponse.builder()
                .transactionId(transaction.getId())
                .bookingId(
                        transaction.getBooking() != null
                                ? transaction.getBooking().getId()
                                : null
                )

                // payer/payee yani Users can be null for system-level or adjustment transactions
                .payerId(transaction.getPayer() != null ? transaction.getPayer().getId() : null)
                .payeeId(transaction.getPayee() != null ? transaction.getPayee().getId() : null)

                // Amounts
                .amount(transaction.getAmount())
                .platformFee(transaction.getPlatformFee())
                .netAmount(transaction.getNetAmount())

                // Payment info
                .currency(transaction.getCurrency())
                .transactionType(transaction.getTransactionType())
                .status(transaction.getStatus())

                // Escrow
                .escrow(transaction.isEscrow())
                .escrowReleaseAt(transaction.getEscrowReleaseAt())

                // Gateway info
                .transactionReference(transaction.getTransactionReference())
                .paymentGateway(transaction.getPaymentGateway())
                .paymentMethod(transaction.getPaymentMethod())

                // Failure / message
                .failureReason(transaction.getFailureReason())
                .message(
                        transaction.getDescription() != null
                                ? transaction.getDescription()
                                : transaction.getStatus().name()
                )

                // Audit
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())

                .build();
    }


}

