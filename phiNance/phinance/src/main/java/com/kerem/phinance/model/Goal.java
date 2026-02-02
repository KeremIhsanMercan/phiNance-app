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
@Document(collection = "goals")
public class Goal {

    @Id
    private String id;

    private String userId;

    private String name;

    private String description;

    private BigDecimal targetAmount;

    private BigDecimal currentAmount = BigDecimal.ZERO;

    private LocalDate deadline;

    private Priority priority;

    private String accountId;

    private List<String> dependencyGoalIds = new ArrayList<>();

    private boolean completed = false;

    private String savingsAccountId;

    private String color;

    private String icon;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH
    }

    public double getProgressPercentage() {
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        if (currentAmount == null) {
            return 0.0;
        }
        return currentAmount.divide(targetAmount, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }
}
