package com.kerem.phinance.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "accounts")
public class Account {

    @Id
    private String id;

    private String userId;

    private String name;

    private AccountType type;

    private BigDecimal initialBalance;

    private BigDecimal currentBalance;

    private String currency;

    private String description;

    private String color;

    private String icon;

    private boolean archived = false;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum AccountType {
        BANK_ACCOUNT,
        CREDIT_CARD,
        CASH,
        INVESTMENT_ACCOUNT
    }
}
