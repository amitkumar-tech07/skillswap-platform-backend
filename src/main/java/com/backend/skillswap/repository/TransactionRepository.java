package com.backend.skillswap.repository;

import com.backend.skillswap.entity.Booking;
import com.backend.skillswap.entity.Transaction;
import com.backend.skillswap.entity.UserEntity;
import com.backend.skillswap.entity.enums.TransactionStatus;
import com.backend.skillswap.entity.enums.TransactionType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // ------------------------- FIND TRANSACTIONS BY USER -------------------------
    List<Transaction> findByPayer(UserEntity payer);   // All payments made by user
    List<Transaction> findByPayee(UserEntity payee);   // All payments received by user
    List<Transaction> findByPayerOrPayee(UserEntity payer, UserEntity payee);

    List<Transaction> findByPayerAndStatus(UserEntity payer, TransactionStatus status);
    List<Transaction> findByPayeeAndStatus(UserEntity payee, TransactionStatus status);

    // ------------------------- FIND TRANSACTIONS BY BOOKING -------------------------
    List<Transaction> findByBooking(Booking booking);

    List<Transaction> findByBookingAndStatus(Booking booking, TransactionStatus status);

    List<Transaction> findByTransactionTypeAndStatus(TransactionType transactionType, TransactionStatus status);

    // ------------------------- SINGLE TRANSACTION BY REFERENCE -------------------------
    Optional<Transaction> findByTransactionReference(String transactionReference);

    // ------------------------- CUSTOM QUERY EXAMPLE -------------------------
    @Query("SELECT t FROM Transaction t WHERE t.payer = :payer AND t.amount > :minAmount ORDER BY t.createdAt DESC")
    List<Transaction> findUserTransactionsAboveAmount(UserEntity payer, java.math.BigDecimal minAmount);


    // ------------------------ Check if escrow already exists for this booking --------------------------------

    boolean existsByBookingAndTransactionTypeAndStatus(
            Booking booking,
            TransactionType type,
            TransactionStatus status
    );

    // -------------------------------- Fetch last transaction attempt for booking -------------------------------
    @Lock(LockModeType.OPTIMISTIC)
    Optional<Transaction> findByBookingAndTransactionTypeAndStatus(
            Booking booking,
            TransactionType type,
            TransactionStatus status
    );

    List<Transaction> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    @Query("""
SELECT COALESCE(SUM(
    CASE
        WHEN t.transactionType = 'DEPOSIT'
             AND t.payee.id = :userId
             AND t.status = 'SUCCESS'
            THEN t.amount
        WHEN t.transactionType = 'WITHDRAW'
             AND t.payer.id = :userId
             AND t.status = 'SUCCESS'
            THEN -t.amount
        WHEN t.transactionType = 'ESCROW'
             AND t.payer.id = :userId
             AND t.status = 'PENDING'
            THEN -t.amount
        WHEN t.transactionType = 'ESCROW'
             AND t.payee.id = :userId
             AND t.status = 'SUCCESS'
            THEN t.netAmount
        WHEN t.transactionType = 'ESCROW'
             AND t.payer.id = :userId
             AND t.status = 'REFUNDED'
            THEN t.amount
        ELSE 0
    END
), 0)
FROM Transaction t
""")
    BigDecimal calculateNetWalletFlow(@Param("userId") Long userId);

}

