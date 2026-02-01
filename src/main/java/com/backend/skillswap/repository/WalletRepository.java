package com.backend.skillswap.repository;

import com.backend.skillswap.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

// Wallet balance is derived from immutable transactions.
@Repository
public interface WalletRepository extends JpaRepository<Transaction, Long> {

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
    BigDecimal calculateWalletBalance(Long userId);

}

