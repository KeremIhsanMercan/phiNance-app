package com.kerem.phinance.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "favorite_filters")
public class FavoriteFilter {

    @Field("_id")
    private String id;

    private String userId;

    private String name;

    private Map<String, String> filters;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
