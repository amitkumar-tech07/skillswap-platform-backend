package com.backend.skillswap.dto.request;

import com.backend.skillswap.entity.enums.Currency;
import com.backend.skillswap.entity.enums.PaymentMethod;
import com.backend.skillswap.entity.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import lombok.*;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest {

    // Booking ID required ONLY for ESCROW / RELEASE / REFUND
    // Wallet transactions (DEPOSIT / WITHDRAW) me null allowed
    private Long bookingId;  // Booking jiske liye payment ho rahi hai

    @NotNull(message = "Transaction type is required (DEPOSIT, ESCROW, RELEASE, REFUND, WITHDRAW)")
    private TransactionType transactionType;   // Payment type like: DEPOSIT / RELEASE / REFUND

    @NotNull(message = "Payment method is required (UPI, CARD, NET_BANKING, WALLET)")
    private PaymentMethod paymentMethod;   // Payment method like: UPI / CARD / NET_BANKING  / WALLET

    private boolean escrow;

    @NotNull(message = "Currency is required (INR, USD, EUR)")
    private Currency currency;

}
