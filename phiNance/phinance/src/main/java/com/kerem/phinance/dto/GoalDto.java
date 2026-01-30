package com.kerem.phinance.dto;

import com.kerem.phinance.model.Goal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalDto {

    private String id;

    @NotBlank(message = "Goal name is required")
    private String name;

    private String description;

    @NotNull(message = "Target amount is required")
    @Positive(message = "Target amount must be positive")
    private BigDecimal targetAmount;

    private BigDecimal currentAmount;

    @NotNull(message = "Deadline is required")
    private LocalDate deadline;

    @NotNull(message = "Priority is required")
    private Goal.Priority priority;

    private String accountId;

    private List<String> dependencyGoalIds;

    private boolean completed;

    private String color;

    private String icon;

    private Double progressPercentage;
}
