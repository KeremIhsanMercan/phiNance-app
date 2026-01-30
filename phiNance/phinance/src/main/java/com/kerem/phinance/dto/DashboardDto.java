package com.kerem.phinance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {

    private BigDecimal totalNetWorth;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private List<AccountSummary> accountSummaries;
    private List<CategoryExpense> categoryExpenses;
    private List<MonthlyData> monthlyData;
    private List<TransactionDto> recentTransactions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountSummary {
        private String id;
        private String name;
        private String type;
        private BigDecimal balance;
        private String currency;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryExpense {
        private String categoryId;
        private String categoryName;
        private String color;
        private BigDecimal amount;
        private double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyData {
        private String month;
        private BigDecimal income;
        private BigDecimal expenses;
    }
}
