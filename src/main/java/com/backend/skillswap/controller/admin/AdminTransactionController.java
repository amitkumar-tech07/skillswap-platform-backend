package com.backend.skillswap.controller.admin;

import com.backend.skillswap.dto.response.TransactionResponse;
import com.backend.skillswap.entity.Booking;
import com.backend.skillswap.entity.UserEntity;
import com.backend.skillswap.entity.enums.TransactionStatus;
import com.backend.skillswap.entity.enums.TransactionType;
import com.backend.skillswap.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "16. Admin Transactions APIs", description = "Admin-level transaction management, reporting, and analytics")
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/transactions")
public class AdminTransactionController {

    private final TransactionService transactionService;

    // ================= ADMIN - TRANSACTIONS =================

    @Operation(summary = "Get all transactions")
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @Operation(summary = "Update transaction status (Admin / Webhook)")
    @PatchMapping("/update-status")
    public ResponseEntity<TransactionResponse> updateTransactionStatus(
            @RequestParam String reference,
            @RequestParam TransactionStatus status
    ) {
        return ResponseEntity.ok(transactionService.updateTransactionStatus(reference, status));
    }

    @Operation(summary = "Get transactions by type & status")
    @GetMapping("/type-status")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByTypeAndStatus(
            @RequestParam TransactionType type,
            @RequestParam TransactionStatus status
    ) {
        return ResponseEntity.ok(transactionService.getTransactionsByTypeAndStatus(type, status));
    }

    @Operation(summary = "Get transactions between dates")
    @GetMapping("/report")
    public ResponseEntity<List<TransactionResponse>> getTransactionsBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        return ResponseEntity.ok(transactionService.getTransactionsBetweenDates(start, end));
    }

    @Operation(summary = "Get transactions by payer")
    @GetMapping("/payer/{payerId}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByPayer(@PathVariable Long payerId) {
        UserEntity payer = new UserEntity();
        payer.setId(payerId);
        return ResponseEntity.ok(transactionService.getTransactionsByPayer(payer));
    }

    @Operation(summary = "Get transactions by payer & status")
    @GetMapping("/payer-status")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByPayerAndStatus(
            @RequestParam Long payerId,
            @RequestParam TransactionStatus status
    ) {
        UserEntity payer = new UserEntity();
        payer.setId(payerId);
        return ResponseEntity.ok(transactionService.getTransactionsByPayerAndStatus(payer, status));
    }

    @Operation(summary = "Get transactions by payee")
    @GetMapping("/payee/{payeeId}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByPayee(@PathVariable Long payeeId) {
        UserEntity payee = new UserEntity();
        payee.setId(payeeId);
        return ResponseEntity.ok(transactionService.getTransactionsByPayee(payee));
    }

    @Operation(summary = "Get transactions by payee & status")
    @GetMapping("/payee-status")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByPayeeAndStatus(
            @RequestParam Long payeeId,
            @RequestParam TransactionStatus status
    ) {
        UserEntity payee = new UserEntity();
        payee.setId(payeeId);
        return ResponseEntity.ok(transactionService.getTransactionsByPayeeAndStatus(payee, status));
    }

    @Operation(summary = "Get transactions by booking & status")
    @GetMapping("/booking-status")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByBookingAndStatus(
            @RequestParam Long bookingId,
            @RequestParam TransactionStatus status
    ) {
        Booking booking = new Booking();
        booking.setId(bookingId);
        return ResponseEntity.ok(transactionService.getTransactionsByBookingAndStatus(booking, status));
    }
}
