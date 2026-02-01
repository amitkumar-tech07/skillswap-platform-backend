package com.backend.skillswap.service.impl;

import com.backend.skillswap.dto.response.TransactionResponse;
import com.backend.skillswap.entity.Booking;
import com.backend.skillswap.entity.Transaction;
import com.backend.skillswap.entity.UserEntity;
import com.backend.skillswap.entity.enums.*;
import com.backend.skillswap.exception.common.InvalidRequestException;
import com.backend.skillswap.exception.common.ResourceNotFoundException;
import com.backend.skillswap.exception.transaction.EscrowNotFoundException;
import com.backend.skillswap.exception.transaction.InsufficientBalanceException;
import com.backend.skillswap.exception.transaction.TransactionAlreadyProcessedException;
import com.backend.skillswap.exception.transaction.TransactionFailedException;
import com.backend.skillswap.mapper.TransactionMapper;
import com.backend.skillswap.repository.BookingRepository;
import com.backend.skillswap.repository.TransactionRepository;
import com.backend.skillswap.repository.WalletRepository;
import com.backend.skillswap.service.EmailService;
import com.backend.skillswap.service.TransactionService;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final BookingRepository bookingRepository;
    private final WalletRepository walletRepository;
    private final EmailService emailService;

    // ================= WALLET =================
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public TransactionResponse deposit(UserEntity user, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRequestException("Amount must be greater than zero");
        }

        int retry = 3;
        while (retry-- > 0) {
            try {
                Transaction tx = Transaction.builder()
                        .payer(user)
                        .payee(user)
                        .amount(amount)
                        .netAmount(amount)
                        .transactionType(TransactionType.DEPOSIT)
                        .status(TransactionStatus.SUCCESS)
                        .paymentGateway(PaymentGateway.INTERNAL)
                        .paymentMethod(PaymentMethod.WALLET)
                        .escrow(false)  // Deposit kabhi escrow nhi hoga
                        .transactionReference(UUID.randomUUID().toString())
                        .build();

                Transaction saved = transactionRepository.save(tx);

                try {
                    emailService.sendTransactionMail(
                            user.getEmail(),
                            "Wallet Deposit Successful",
                            "₹" + amount + " has been deposited to your wallet."
                    );
                } catch (Exception e) {
                    log.error("Email failed for deposit tx {}", saved.getId(), e);
                }

                return TransactionMapper.toResponse(saved);

            } catch (OptimisticLockException e) {
                if (retry == 0) throw e;
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            }
        }
        throw new RuntimeException("Failed to deposit due to concurrent updates");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public TransactionResponse withdraw(UserEntity user, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRequestException("Amount must be greater than zero");
        }

        int retry = 3;
        while (retry-- > 0) {
            try {
                BigDecimal balance = getWalletBalance(user);
                if (balance.compareTo(amount) < 0) {
                    throw new InsufficientBalanceException("Insufficient wallet balance");
                }

                Transaction tx = Transaction.builder()
                        .payer(user)
                        .payee(user)
                        .amount(amount)
                        .netAmount(BigDecimal.ZERO)
                        .transactionType(TransactionType.WITHDRAW)
                        .status(TransactionStatus.SUCCESS)
                        .paymentGateway(PaymentGateway.INTERNAL)
                        .paymentMethod(PaymentMethod.WALLET)
                        .escrow(false)
                        .transactionReference(UUID.randomUUID().toString())
                        .build();

                Transaction saved = transactionRepository.save(tx);

                emailService.sendTransactionMail(user.getEmail(),
                        "Wallet Withdrawal Successful",
                        "₹" + amount + " has been withdrawn from your wallet.");

                return TransactionMapper.toResponse(saved);

            } catch (OptimisticLockException e) {
                if (retry == 0) throw e;
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            }
        }
        throw new RuntimeException("Failed to withdraw due to concurrent updates");
    }

    @Override
    public BigDecimal getWalletBalance(UserEntity user) {
        BigDecimal balance = walletRepository.calculateWalletBalance(user.getId());
        return balance != null ? balance : BigDecimal.ZERO;
    }

    @Override
    public List<TransactionResponse> getUserTransactions(UserEntity user) {
        return transactionRepository.findByPayerOrPayee(user, user)
                .stream()
                .map(TransactionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAll()
                .stream()
                .map(TransactionMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ================= ESCROW / BOOKING =================
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public TransactionResponse createEscrowTransaction(UserEntity payer, Booking booking, BigDecimal amount, TransactionType type) {

        // Money lock sirf CONFIRMED pe , PENDING ya IN_PROGRESS pe kabhi escrow nhi hoga
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidRequestException("Escrow can be created only on CONFIRMED booking");
        }

        TransactionType txType = (type != null) ? type : TransactionType.ESCROW;

        // Payee is always provider of the booking
        UserEntity payee = booking.getProvider();

        int retry = 3;
        while (retry-- > 0) {
            try {
                if (transactionRepository.existsByBookingAndTransactionTypeAndStatus( booking, TransactionType.ESCROW, TransactionStatus.PENDING)) {
                    throw new TransactionAlreadyProcessedException("Escrow already exists");
                }

                BigDecimal balance = getWalletBalance(payer);
                if (balance.compareTo(amount) < 0) {
                    throw new InsufficientBalanceException("Not enough balance to create escrow");
                }

                Transaction escrow = Transaction.builder()
                        .payer(payer)
                        .payee(payee)
                        .booking(booking)
                        .amount(amount)
                        .netAmount(BigDecimal.ZERO)
                        .transactionType(txType)
                        .status(TransactionStatus.PENDING)
                        .paymentGateway(PaymentGateway.INTERNAL)
                        .paymentMethod(PaymentMethod.WALLET)
                        .escrow(true)
                        .transactionReference(UUID.randomUUID().toString())
                        .build();

                Transaction saved = transactionRepository.save(escrow);

                emailService.sendTransactionMail(payer.getEmail(),
                        "Escrow Created",
                        "₹" + amount + " has been locked in escrow for booking ID: " + booking.getId());

                return TransactionMapper.toResponse(saved);

            } catch (OptimisticLockException e) {
                if (retry == 0) throw e;
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            }
        }
        throw new RuntimeException("Failed to create escrow due to concurrent updates");
    }

    @Transactional
    @Override
    public TransactionResponse releaseEscrow(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new InvalidRequestException("Booking must be COMPLETED to release escrow");
        }

        int retry = 3;
        while (retry-- > 0) {
            try {
                // Fetch only PENDING escrow transaction for booking
                Transaction escrow = transactionRepository.findByBookingAndTransactionTypeAndStatus(
                        booking,
                        TransactionType.ESCROW,
                        TransactionStatus.PENDING
                ).orElseThrow(() ->
                        new EscrowNotFoundException(
                                "No pending escrow found for booking ID: " + booking.getId()
                        ));

                // Mark escrow as SUCCESS
                escrow.setStatus(TransactionStatus.SUCCESS);
                transactionRepository.save(escrow);

                // Create RELEASE transaction
                Transaction release = Transaction.builder()
                        .payer(escrow.getPayer())
                        .payee(escrow.getPayee())
                        .booking(booking)
                        .amount(escrow.getAmount())
                        .netAmount(escrow.getAmount())
                        .transactionType(TransactionType.RELEASE)
                        .status(TransactionStatus.SUCCESS)
                        .paymentGateway(PaymentGateway.INTERNAL)
                        .paymentMethod(PaymentMethod.WALLET)
                        .escrow(false)
                        .transactionReference(UUID.randomUUID().toString())
                        .build();

                Transaction savedRelease = transactionRepository.save(release);

                // Async email
                emailService.sendTransactionMail(
                        savedRelease.getPayee().getEmail(),
                        "Escrow Released",
                        "₹" + savedRelease.getAmount()
                                + " has been released for booking ID: " + booking.getId()
                );

                return TransactionMapper.toResponse(savedRelease);

            } catch (OptimisticLockException e) {
                if (retry == 0) {
                    throw new TransactionFailedException(
                            "Failed to release escrow after concurrent retries"
                    );
                }
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            }
        }
        throw new TransactionFailedException("Failed to release escrow due to unknown error");
    }

    @Transactional
    @Override
    public TransactionResponse refund(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        // COMPLETED booking kabhi refund nahi hogi, Refund = ONLY CANCELLED
        if (booking.getStatus() != BookingStatus.CANCELLED) {
            throw new InvalidRequestException(
                    "Booking must be CANCELLED to refund escrow"
            );
        }

        int retry = 3;
        while (retry-- > 0) {
            try {
                Transaction escrow =
                        transactionRepository.findByBookingAndTransactionTypeAndStatus(
                                booking,
                                TransactionType.ESCROW,
                                TransactionStatus.PENDING
                        ).orElseThrow(() ->
                                new EscrowNotFoundException("No pending escrow found")
                        );

                // Idempotent check
                if (escrow.getStatus() != TransactionStatus.PENDING) {
                    throw new TransactionAlreadyProcessedException("Refund already processed for booking ID: " + booking.getId());
                }

                // Mark escrow refunded
                escrow.setStatus(TransactionStatus.REFUNDED);
                transactionRepository.save(escrow);

                UserEntity requester = booking.getRequester();

                // Create REFUND transaction
                Transaction refund = Transaction.builder()
                        .payer(requester)
                        .payee(requester)
                        .booking(booking)
                        .amount(escrow.getAmount())
                        .netAmount(escrow.getAmount())
                        .transactionType(TransactionType.REFUND)
                        .status(TransactionStatus.SUCCESS)
                        .paymentGateway(PaymentGateway.INTERNAL)
                        .paymentMethod(PaymentMethod.WALLET)
                        .escrow(false)
                        .transactionReference(UUID.randomUUID().toString())
                        .build();

                Transaction savedRefund = transactionRepository.save(refund);

                // Async mail
                emailService.sendTransactionMail(
                        savedRefund.getPayer().getEmail(),
                        "Booking Cancelled / Refund Successful",
                        "₹" + savedRefund.getAmount()
                                + " has been refunded for booking ID: " + booking.getId()
                );

                return TransactionMapper.toResponse(savedRefund);

            } catch (OptimisticLockException e) {
                if (retry == 0) {
                    throw new TransactionFailedException("Failed to refund escrow after concurrent retries");
                }
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            }
        }
        throw new TransactionFailedException("Refund failed due to unknown error");
    }

    // ================= FETCH =================
    @Override
    public TransactionResponse getTransactionById(Long transactionId) {
        return transactionRepository.findById(transactionId).map(TransactionMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transactionId));
    }

    @Override
    public TransactionResponse getTransactionByReference(String reference) {
        return transactionRepository.findByTransactionReference(reference)
                .map(TransactionMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid transaction reference: " + reference));
    }

    @Override
    public List<TransactionResponse> getTransactionsByPayer(UserEntity payer) {
        return transactionRepository.findByPayer(payer)
                .stream().map(TransactionMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> getTransactionsByPayee(UserEntity payee) {
        return transactionRepository.findByPayee(payee)
                .stream().map(TransactionMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> getTransactionsByBooking(Booking booking) {
        return transactionRepository.findByBooking(booking)
                .stream().map(TransactionMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> getUserTransactionsAboveAmount(UserEntity payer, BigDecimal minAmount) {
        return transactionRepository.findUserTransactionsAboveAmount(payer, minAmount)
                .stream().map(TransactionMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> getTransactionsByTypeAndStatus(TransactionType type, TransactionStatus status) {
        return transactionRepository.findByTransactionTypeAndStatus(type, status)
                .stream().map(TransactionMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> getTransactionsByBookingAndStatus(Booking booking, TransactionStatus status) {
        return transactionRepository.findByBookingAndStatus(booking, status)
                .stream().map(TransactionMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> getTransactionsByPayerAndStatus(UserEntity payer, TransactionStatus status) {
        return transactionRepository.findByPayerAndStatus(payer, status)
                .stream().map(TransactionMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> getTransactionsByPayeeAndStatus(UserEntity payee, TransactionStatus status) {
        return transactionRepository.findByPayeeAndStatus(payee, status)
                .stream().map(TransactionMapper::toResponse).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public TransactionResponse updateTransactionStatus(String reference, TransactionStatus status) {
        Transaction tx = transactionRepository.findByTransactionReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (tx.getStatus() != status) {
            tx.setStatus(status);
            tx = transactionRepository.save(tx);

            if (tx.getPayee() != null) {
                emailService.sendTransactionMail(tx.getPayee().getEmail(),
                        "Transaction Status Updated",
                        "Your transaction " + tx.getTransactionReference() + " is now " + status);
            }
        }

        return TransactionMapper.toResponse(tx);
    }

    @Override
    public List<TransactionResponse> getTransactionsBetweenDates(LocalDateTime start, LocalDateTime end) {
        // Null validation
        if (start == null || end == null) {
            throw new InvalidRequestException("Start date and end date are required");
        }

        // Logical validation
        if (start.isAfter(end)) {
            throw new InvalidRequestException("Start date must be before end date");
        }

        //  Fetch data
        List<Transaction> transactions = transactionRepository.findByCreatedAtBetween(start, end);

        // Empty data handling
        if (transactions.isEmpty()) {
            throw new ResourceNotFoundException("No transactions found between " + start + " and " + end);
        }

        // Mapping
        return transactions.stream()
                .map(TransactionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal getNetWalletFlow(UserEntity user) {
        BigDecimal netFlow = transactionRepository.calculateNetWalletFlow(user.getId());
        log.debug("Net wallet flow for user {} = {}", user.getId(), netFlow);
        return netFlow != null ? netFlow : BigDecimal.ZERO;
    }
}
