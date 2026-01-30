package com.kerem.phinance.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "saved_filters")
public class SavedFilter {

    @Id
    private String id;

    private String userId;

    private String name;

    private Map<String, Object> filterCriteria;

    @CreatedDate
    private LocalDateTime createdAt;
}
