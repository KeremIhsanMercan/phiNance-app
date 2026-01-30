package com.kerem.phinance.controller;

import com.kerem.phinance.dto.GoalContributionDto;
import com.kerem.phinance.dto.GoalDto;
import com.kerem.phinance.security.UserPrincipal;
import com.kerem.phinance.service.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<List<GoalDto>> getAllGoals(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(goalService.getAllGoals(userPrincipal.getId()));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active goals")
    public ResponseEntity<List<GoalDto>> getActiveGoals(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(goalService.getActiveGoals(userPrincipal.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get goal by ID")
    public ResponseEntity<GoalDto> getGoalById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id) {
        return ResponseEntity.ok(goalService.getGoalById(userPrincipal.getId(), id));
    }

    @GetMapping("/{id}/validate-dependencies")
    @Operation(summary = "Validate goal dependencies")
    public ResponseEntity<Map<String, Boolean>> validateGoalDependencies(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id) {
        boolean valid = goalService.validateGoalDependencies(userPrincipal.getId(), id);
        return ResponseEntity.ok(Map.of("dependenciesMet", valid));
    }

    @PostMapping
    @Operation(summary = "Create a new goal")
    public ResponseEntity<GoalDto> createGoal(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody GoalDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(goalService.createGoal(userPrincipal.getId(), dto));
    }

    @PostMapping("/contribution")
    @Operation(summary = "Add contribution to a goal")
    public ResponseEntity<GoalDto> addContribution(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody GoalContributionDto dto) {
        return ResponseEntity.ok(goalService.addContribution(userPrincipal.getId(), dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a goal")
    public ResponseEntity<GoalDto> updateGoal(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id,
            @Valid @RequestBody GoalDto dto) {
        return ResponseEntity.ok(goalService.updateGoal(userPrincipal.getId(), id, dto));
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "Mark goal as completed")
    public ResponseEntity<GoalDto> markAsCompleted(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id) {
        return ResponseEntity.ok(goalService.markAsCompleted(userPrincipal.getId(), id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a goal")
    public ResponseEntity<Map<String, String>> deleteGoal(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id) {
        goalService.deleteGoal(userPrincipal.getId(), id);
        return ResponseEntity.ok(Map.of("message", "Goal deleted successfully"));
    }
}
