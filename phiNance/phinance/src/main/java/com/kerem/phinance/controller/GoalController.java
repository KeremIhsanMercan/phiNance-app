package com.kerem.phinance.controller;

import com.kerem.phinance.dto.GoalContributionDto;
import com.kerem.phinance.dto.GoalDto;
import com.kerem.phinance.service.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
@Tag(name = "Goals", description = "Financial goals management APIs")
public class GoalController {

    private final GoalService goalService;

    @GetMapping
    @Operation(summary = "Get all goals")
    public ResponseEntity<Page<GoalDto>> getAllGoals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(goalService.getGoalsPaginated(pageable));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active goals")
    public ResponseEntity<List<GoalDto>> getActiveGoals() {
        return ResponseEntity.ok(goalService.getActiveGoals());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get goal by ID")
    public ResponseEntity<GoalDto> getGoalById(
            @PathVariable String id) {
        return ResponseEntity.ok(goalService.getGoalById(id));
    }

    @GetMapping("/{id}/validate-dependencies")
    @Operation(summary = "Validate goal dependencies")
    public ResponseEntity<Map<String, Boolean>> validateGoalDependencies(
            @PathVariable String id) {
        boolean valid = goalService.validateGoalDependencies(id);
        return ResponseEntity.ok(Map.of("dependenciesMet", valid));
    }

    @PostMapping
    @Operation(summary = "Create a new goal")
    public ResponseEntity<GoalDto> createGoal(
            @Valid @RequestBody GoalDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(goalService.createGoal(dto));
    }

    @PostMapping("/contribution")
    @Operation(summary = "Add contribution to a goal")
    public ResponseEntity<GoalDto> addContribution(
            @Valid @RequestBody GoalContributionDto dto) {
        return ResponseEntity.ok(goalService.addContribution(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a goal")
    public ResponseEntity<GoalDto> updateGoal(
            @PathVariable String id,
            @Valid @RequestBody GoalDto dto) {
        return ResponseEntity.ok(goalService.updateGoal(id, dto));
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "Mark goal as completed")
    public ResponseEntity<GoalDto> markAsCompleted(
            @PathVariable String id) {
        return ResponseEntity.ok(goalService.markAsCompleted(id));
    }

    @PostMapping("/{id}/dependencies/{dependencyId}")
    @Operation(summary = "Add dependency to a goal")
    public ResponseEntity<GoalDto> addDependency(
            @PathVariable String id,
            @PathVariable String dependencyId) {
        return ResponseEntity.ok(goalService.addDependency(id, dependencyId));
    }

    @DeleteMapping("/{id}/dependencies/{dependencyId}")
    @Operation(summary = "Remove dependency from a goal")
    public ResponseEntity<GoalDto> removeDependency(
            @PathVariable String id,
            @PathVariable String dependencyId) {
        return ResponseEntity.ok(goalService.removeDependency(id, dependencyId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a goal")
    public ResponseEntity<Map<String, String>> deleteGoal(
            @PathVariable String id) {
        goalService.deleteGoal(id);
        return ResponseEntity.ok(Map.of("message", "Goal deleted successfully"));
    }
}
