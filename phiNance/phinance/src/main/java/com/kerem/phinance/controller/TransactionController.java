package com.kerem.phinance.controller;

import com.kerem.phinance.dto.TransactionDto;
import com.kerem.phinance.dto.TransactionFilterDto;
import com.kerem.phinance.security.UserPrincipal;
import com.kerem.phinance.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Transaction management APIs")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    @Operation(summary = "Get all transactions with pagination and filters")
    public ResponseEntity<Page<TransactionDto>> getTransactions(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @ModelAttribute TransactionFilterDto filter) {
        return ResponseEntity.ok(transactionService.getTransactions(userPrincipal.getId(), filter));
    }

    @GetMapping("/export")
    @Operation(summary = "Get all transactions matching filters for export (no pagination)")
    public ResponseEntity<List<TransactionDto>> getTransactionsForExport(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @ModelAttribute TransactionFilterDto filter) {
        return ResponseEntity.ok(transactionService.getTransactionsForExport(userPrincipal.getId(), filter));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID")
    public ResponseEntity<TransactionDto> getTransactionById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id) {
        return ResponseEntity.ok(transactionService.getTransactionById(userPrincipal.getId(), id));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get transactions by date range")
    public ResponseEntity<List<TransactionDto>> getTransactionsByDateRange(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(transactionService.getTransactionsByDateRange(
                userPrincipal.getId(), startDate, endDate));
    }

    @PostMapping
    @Operation(summary = "Create a new transaction")
    public ResponseEntity<TransactionDto> createTransaction(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody TransactionDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.createTransaction(userPrincipal.getId(), dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a transaction")
    public ResponseEntity<TransactionDto> updateTransaction(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id,
            @Valid @RequestBody TransactionDto dto) {
        return ResponseEntity.ok(transactionService.updateTransaction(userPrincipal.getId(), id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a transaction")
    public ResponseEntity<Map<String, String>> deleteTransaction(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id) {
        transactionService.deleteTransaction(userPrincipal.getId(), id);
        return ResponseEntity.ok(Map.of("message", "Transaction deleted successfully"));
    }
}
