package com.backend.skillswap.service;

import com.backend.skillswap.dto.response.TransactionResponse;
import com.backend.skillswap.entity.Booking;
import com.backend.skillswap.entity.UserEntity;
import com.backend.skillswap.entity.enums.TransactionStatus;
import com.backend.skillswap.entity.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {

    // ================= WALLET =================

    TransactionResponse deposit(UserEntity user, BigDecimal amount);

    TransactionResponse withdraw(UserEntity user, BigDecimal amount);

    BigDecimal getWalletBalance(UserEntity user);

    List<TransactionResponse> getUserTransactions(UserEntity user);

    List<TransactionResponse> getAllTransactions();

    // ================= ESCROW =================

    // Service
    TransactionResponse createEscrowTransaction(UserEntity payer, Booking booking, BigDecimal amount, TransactionType type);

    TransactionResponse refund(Long bookingId);

    TransactionResponse releaseEscrow(Long bookingId);

    TransactionResponse updateTransactionStatus(
            String transactionReference,
            TransactionStatus status
    );

    // ================= FETCH =================

    TransactionResponse getTransactionById(Long transactionId);

    TransactionResponse getTransactionByReference(String reference);

    List<TransactionResponse> getTransactionsByPayer(UserEntity payer);

    List<TransactionResponse> getTransactionsByPayee(UserEntity payee);

    List<TransactionResponse> getTransactionsByBooking(Booking booking);

    // ================= ADVANCED QUERIES =================

    List<TransactionResponse> getUserTransactionsAboveAmount(
            UserEntity payer,
            BigDecimal minAmount
    );

    List<TransactionResponse> getTransactionsByTypeAndStatus(
            TransactionType type,
            TransactionStatus status
    );

    List<TransactionResponse> getTransactionsByBookingAndStatus(
            Booking booking,
            TransactionStatus status
    );

    List<TransactionResponse> getTransactionsByPayerAndStatus(
            UserEntity payer,
            TransactionStatus status
    );

    List<TransactionResponse> getTransactionsByPayeeAndStatus(
            UserEntity payee,
            TransactionStatus status
    );

    // ================= REPORTING =================

    List<TransactionResponse> getTransactionsBetweenDates(
            LocalDateTime start,
            LocalDateTime end
    );

    BigDecimal getNetWalletFlow(UserEntity user);
}
