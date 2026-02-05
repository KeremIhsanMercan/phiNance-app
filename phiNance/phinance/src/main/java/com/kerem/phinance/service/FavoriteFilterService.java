package com.kerem.phinance.service;

import com.kerem.phinance.dto.FavoriteFilterDTO;
import com.kerem.phinance.exception.BadRequestException;
import com.kerem.phinance.model.FavoriteFilter;
import com.kerem.phinance.repository.FavoriteFilterRepository;
import com.kerem.phinance.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteFilterService {

    private final FavoriteFilterRepository favoriteFilterRepository;

    /**
     * Get all favorite filters for the authenticated user
     */
    public List<FavoriteFilterDTO> getAllFilters() {
        String userId = SecurityUtils.getCurrentUserId();
        return favoriteFilterRepository.findByUserId(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Save a new favorite filter
     */
    public FavoriteFilterDTO saveFilter(String name, Map<String, String> filters) {
        String userId = SecurityUtils.getCurrentUserId();

        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Filter name cannot be empty");
        }

        if (filters == null || filters.isEmpty()) {
            throw new BadRequestException("Filters cannot be empty");
        }

        FavoriteFilter favoriteFilter = new FavoriteFilter();
        favoriteFilter.setId(UUID.randomUUID().toString());
        favoriteFilter.setUserId(userId);
        favoriteFilter.setName(name);
        favoriteFilter.setFilters(filters);
        favoriteFilter.setCreatedAt(LocalDateTime.now());
        favoriteFilter.setUpdatedAt(LocalDateTime.now());

        FavoriteFilter saved = favoriteFilterRepository.save(favoriteFilter);
        log.info("Favorite filter saved with ID: {} for user: {}", saved.getId(), userId);

        return convertToDTO(saved);
    }

    /**
     * Delete a favorite filter
     */
    public void deleteFilter(String filterId) {
        String userId = SecurityUtils.getCurrentUserId();

        FavoriteFilter filter = favoriteFilterRepository.findByIdAndUserId(filterId, userId)
                .orElseThrow(() -> new BadRequestException("Filter not found"));

        favoriteFilterRepository.deleteByIdAndUserId(filterId, userId);
        log.info("Favorite filter deleted with ID: {} for user: {}", filterId, userId);
    }

    /**
     * Convert FavoriteFilter entity to DTO
     */
    private FavoriteFilterDTO convertToDTO(FavoriteFilter filter) {
        return new FavoriteFilterDTO(
                filter.getId(),
                filter.getName(),
                filter.getFilters(),
                filter.getCreatedAt()
        );
    }
}
