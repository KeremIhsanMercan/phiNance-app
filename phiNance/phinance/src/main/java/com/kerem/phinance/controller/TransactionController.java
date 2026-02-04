package com.kerem.phinance.controller;

import com.kerem.phinance.dto.TransactionDto;
import com.kerem.phinance.dto.TransactionFilterDto;
import com.kerem.phinance.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            @ModelAttribute TransactionFilterDto filter) {
        return ResponseEntity.ok(transactionService.getTransactions(filter));
    }

    @GetMapping("/export")
    @Operation(summary = "Get all transactions matching filters for export (no pagination)")
    public ResponseEntity<List<TransactionDto>> getTransactionsForExport(
            @ModelAttribute TransactionFilterDto filter) {
        return ResponseEntity.ok(transactionService.getTransactionsForExport(filter));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID")
    public ResponseEntity<TransactionDto> getTransactionById(
            @PathVariable String id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get transactions by date range")
    public ResponseEntity<List<TransactionDto>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(transactionService.getTransactionsByDateRange(
                startDate, endDate));
    }

    @PostMapping
    @Operation(summary = "Create a new transaction")
    public ResponseEntity<TransactionDto> createTransaction(
            @Valid @RequestBody TransactionDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.createTransaction(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a transaction")
    public ResponseEntity<TransactionDto> updateTransaction(
            @PathVariable String id,
            @Valid @RequestBody TransactionDto dto) {
        return ResponseEntity.ok(transactionService.updateTransaction(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a transaction")
    public ResponseEntity<Map<String, String>> deleteTransaction(
            @PathVariable String id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok(Map.of("message", "Transaction deleted successfully"));
    }
}
