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
import java.time.YearMonth;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "budgets")
public class Budget {

    @Id
    private String id;

    private String userId;

    private String categoryId;

    private BigDecimal allocatedAmount;

    private BigDecimal spentAmount = BigDecimal.ZERO;

    private int year;

    private int month;

    private int alertThreshold = 80;

    private boolean alertAt80Sent = false;

    private boolean alertAt100Sent = false;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public BigDecimal getRemainingAmount() {
        if (allocatedAmount == null) {
            return BigDecimal.ZERO;
        }
        return allocatedAmount.subtract(spentAmount != null ? spentAmount : BigDecimal.ZERO);
    }

    public double getSpentPercentage() {
        if (allocatedAmount == null || allocatedAmount.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return spentAmount.divide(allocatedAmount, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    public YearMonth getPeriod() {
        return YearMonth.of(year, month);
    }
}
