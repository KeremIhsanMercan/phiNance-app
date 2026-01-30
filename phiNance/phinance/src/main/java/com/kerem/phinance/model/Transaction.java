package com.kerem.phinance.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "transactions")
public class Transaction {

    @Id
    private String id;

    private String userId;

    private String accountId;

    private TransactionType type;

    private BigDecimal amount;

    private String categoryId;

    private String description;

    private LocalDate date;

    private boolean recurring = false;

    private RecurrencePattern recurrencePattern;

    private String transferToAccountId;

    private String linkedTransactionId;

    private List<String> attachmentUrls = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum TransactionType {
        INCOME,
        EXPENSE,
        TRANSFER
    }

    public enum RecurrencePattern {
        DAILY,
        WEEKLY,
        MONTHLY,
        YEARLY
    }
}
