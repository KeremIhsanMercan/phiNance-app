package com.kerem.phinance.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetDto {

    private String id;

    @NotNull(message = "Category ID is required")
    private String categoryId;

    @NotNull(message = "Allocated amount is required")
    @Positive(message = "Allocated amount must be positive")
    private BigDecimal allocatedAmount;

    private BigDecimal spentAmount;

    private BigDecimal remainingAmount;

    @NotNull(message = "Year is required")
    private Integer year;

    @NotNull(message = "Month is required")
    private Integer month;

    private Integer alertThreshold;

    private Double spentPercentage;
}
