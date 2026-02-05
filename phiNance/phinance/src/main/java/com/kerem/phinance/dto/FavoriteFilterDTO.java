package com.kerem.phinance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteFilterDTO {

    private String id;

    private String name;

    private Map<String, String> filters;

    private LocalDateTime createdAt;
}
