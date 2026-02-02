package com.kerem.phinance.dto;

import com.kerem.phinance.model.Transaction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDto {

    private String id;

    @NotNull(message = "Account ID is required")
    private String accountId;

    @NotNull(message = "Transaction type is required")
    private Transaction.TransactionType type;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private String categoryId;

    private String description;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @Builder.Default
    private boolean recurring = false;

    private Transaction.RecurrencePattern recurrencePattern;

    private String transferToAccountId;

    private List<String> attachmentUrls;
}
