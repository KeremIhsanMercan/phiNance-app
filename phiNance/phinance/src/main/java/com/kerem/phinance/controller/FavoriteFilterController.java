package com.kerem.phinance.controller;

import com.kerem.phinance.dto.FavoriteFilterDTO;
import com.kerem.phinance.service.FavoriteFilterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/favorite-filters")
@RequiredArgsConstructor
public class FavoriteFilterController {

    private final FavoriteFilterService favoriteFilterService;

    /**
     * Get all favorite filters for the authenticated user
     */
    @GetMapping
    public ResponseEntity<List<FavoriteFilterDTO>> getAllFilters() {
        List<FavoriteFilterDTO> filters = favoriteFilterService.getAllFilters();
        return ResponseEntity.ok(filters);
    }

    /**
     * Save a new favorite filter Request body: { "name": "filter name",
     * "filters": { "type": "EXPENSE", "category": "123", ... } }
     */
    @PostMapping
    public ResponseEntity<FavoriteFilterDTO> saveFilter(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        @SuppressWarnings("unchecked")
        Map<String, String> filters = (Map<String, String>) request.get("filters");

        FavoriteFilterDTO savedFilter = favoriteFilterService.saveFilter(name, filters);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedFilter);
    }

    /**
     * Delete a favorite filter by ID
     */
    @DeleteMapping("/{filterId}")
    public ResponseEntity<Void> deleteFilter(@PathVariable String filterId) {
        favoriteFilterService.deleteFilter(filterId);
        return ResponseEntity.noContent().build();
    }
}
