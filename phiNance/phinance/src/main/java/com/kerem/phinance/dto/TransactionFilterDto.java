package com.kerem.phinance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionFilterDto {

    private LocalDate startDate;
    private LocalDate endDate;
    private String accountId;
    private String categoryId;
    private String type;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String searchQuery;
    private String sortBy = "date";
    private String sortDirection = "desc";
    private int page = 0;
    private int size = 20;
}
