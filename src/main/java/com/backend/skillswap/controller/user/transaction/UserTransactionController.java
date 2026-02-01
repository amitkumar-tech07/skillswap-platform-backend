package com.backend.skillswap.controller.user.transaction;

import com.backend.skillswap.dto.common.ApiMessageResponse;
import com.backend.skillswap.dto.response.TransactionResponse;
import com.backend.skillswap.entity.Booking;
import com.backend.skillswap.entity.UserEntity;
import com.backend.skillswap.entity.enums.TransactionType;
import com.backend.skillswap.security.SecurityUtil;
import com.backend.skillswap.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/user/transactions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "User Transactions APIs", description = "Wallet, Escrow, Transactions & Analytics")
public class UserTransactionController {

    private final TransactionService transactionService;

    // ================= WALLET =================

    @Operation(summary = "Deposit money into wallet")
    @PostMapping("/wallet/deposit")
    public ResponseEntity<TransactionResponse> deposit(@RequestParam BigDecimal amount) {
        UserEntity user = SecurityUtil.getCurrentUser();
        return ResponseEntity.ok(transactionService.deposit(user, amount));
    }

    @Operation(summary = "Withdraw money from wallet")
    @PostMapping("/wallet/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(@RequestParam BigDecimal amount) {
        UserEntity user = SecurityUtil.getCurrentUser();
        return ResponseEntity.ok(transactionService.withdraw(user, amount));
    }

    @Operation(summary = "Get wallet balance")
    @GetMapping("/wallet/balance")
    public ResponseEntity<ApiMessageResponse> getWalletBalance() {
        UserEntity user = SecurityUtil.getCurrentUser();
        return ResponseEntity.ok(
                new ApiMessageResponse(
                        "Your available wallet balance is : " +
                                transactionService.getWalletBalance(user)
                )
        );
    }

    @Operation(
            summary = "Get net wallet flow",
            description = """
        Returns the net wallet flow for the currently authenticated user.

        Net Flow = Total Credits − Total Debits

        Includes:
        • Wallet deposits
        • Wallet withdrawals
        • Escrow locks
        • Escrow releases
        • Escrow refunds

        This endpoint does NOT accept userId and always uses
        the logged-in user's identity for security.
        """
    )
    @GetMapping("/wallet/net-flow")
    public ResponseEntity<BigDecimal> getNetWalletFlow() {
        UserEntity user = SecurityUtil.getCurrentUser();
        return ResponseEntity.ok(transactionService.getNetWalletFlow(user));
    }

    // ================= USER TRANSACTIONS =================

    @Operation(summary = "Get my transactions")
    @GetMapping("/my")
    public ResponseEntity<List<TransactionResponse>> getMyTransactions() {
        UserEntity user = SecurityUtil.getCurrentUser();
        return ResponseEntity.ok(transactionService.getUserTransactions(user));
    }

    @Operation(summary = "Get my transactions above amount")
    @GetMapping("/my/above-amount")
    public ResponseEntity<List<TransactionResponse>> getMyTransactionsAboveAmount(
            @RequestParam BigDecimal minAmount
    ) {
        UserEntity user = SecurityUtil.getCurrentUser();
        return ResponseEntity.ok(
                transactionService.getUserTransactionsAboveAmount(user, minAmount)
        );
    }

    // ================= ESCROW =================

    @Operation(summary = "Lock escrow for booking")
    @PostMapping("/escrow/lock")
    public ResponseEntity<TransactionResponse> lockEscrow(
            @RequestParam Long bookingId,
            @RequestParam BigDecimal amount
    ) {
        UserEntity payer = SecurityUtil.getCurrentUser();
        Booking booking = new Booking();
        booking.setId(bookingId);

        return ResponseEntity.ok(
                transactionService.createEscrowTransaction(
                        payer,
                        booking,
                        amount,
                        TransactionType.ESCROW
                )
        );
    }

    @Operation(summary = "Release escrow after booking completion")
    @PostMapping("/escrow/release")
    public ResponseEntity<TransactionResponse> releaseEscrow(@RequestParam Long bookingId) {
        return ResponseEntity.ok(transactionService.releaseEscrow(bookingId));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Escrow refunded successfully"),
            @ApiResponse(responseCode = "400", description = "Booking not cancelled / invalid request"),
            @ApiResponse(responseCode = "404", description = "Booking or escrow not found"),
            @ApiResponse(responseCode = "409", description = "Escrow refund already processed"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/escrow/refund")
    public ResponseEntity<TransactionResponse> refundEscrow(@RequestParam Long bookingId) {
        return ResponseEntity.ok(transactionService.refund(bookingId));
    }

    // ================= FETCH =================

    @Operation(summary = "Get transaction by ID")
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getById(@PathVariable Long transactionId) {
        return ResponseEntity.ok(transactionService.getTransactionById(transactionId));
    }

    @Operation(summary = "Get transaction by reference")
    @GetMapping("/reference/{reference}")
    public ResponseEntity<TransactionResponse> getByReference(@PathVariable String reference) {
        return ResponseEntity.ok(transactionService.getTransactionByReference(reference));
    }

    @Operation(summary = "Get transactions by booking")
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<TransactionResponse>> getByBooking(@PathVariable Long bookingId) {
        Booking booking = new Booking();
        booking.setId(bookingId);
        return ResponseEntity.ok(transactionService.getTransactionsByBooking(booking));
    }
}
