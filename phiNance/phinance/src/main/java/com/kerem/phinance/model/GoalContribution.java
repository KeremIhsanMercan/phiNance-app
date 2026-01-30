package com.kerem.phinance.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "goal_contributions")
public class GoalContribution {

    @Id
    private String id;

    private String goalId;

    private String userId;

    private BigDecimal amount;

    private String note;

    @CreatedDate
    private LocalDateTime createdAt;
}
