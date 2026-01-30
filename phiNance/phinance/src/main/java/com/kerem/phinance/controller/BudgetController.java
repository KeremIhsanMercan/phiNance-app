package com.kerem.phinance.controller;

import com.kerem.phinance.dto.BudgetDto;
import com.kerem.phinance.security.UserPrincipal;
import com.kerem.phinance.service.BudgetService;
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
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@Tag(name = "Budgets", description = "Budget management APIs")
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    @Operation(summary = "Get budgets by month")
    public ResponseEntity<List<BudgetDto>> getBudgetsByMonth(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(budgetService.getBudgetsByMonth(userPrincipal.getId(), year, month));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get budget by ID")
    public ResponseEntity<BudgetDto> getBudgetById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id) {
        return ResponseEntity.ok(budgetService.getBudgetById(userPrincipal.getId(), id));
    }

    @GetMapping("/compare")
    @Operation(summary = "Compare budgets between two months")
    public ResponseEntity<List<BudgetDto>> compareBudgets(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam int year1,
            @RequestParam int month1,
            @RequestParam int year2,
            @RequestParam int month2) {
        return ResponseEntity.ok(budgetService.compareBudgets(
                userPrincipal.getId(), year1, month1, year2, month2));
    }

    @PostMapping
    @Operation(summary = "Create a new budget")
    public ResponseEntity<BudgetDto> createBudget(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody BudgetDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(budgetService.createBudget(userPrincipal.getId(), dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a budget")
    public ResponseEntity<BudgetDto> updateBudget(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id,
            @Valid @RequestBody BudgetDto dto) {
        return ResponseEntity.ok(budgetService.updateBudget(userPrincipal.getId(), id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a budget")
    public ResponseEntity<Map<String, String>> deleteBudget(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id) {
        budgetService.deleteBudget(userPrincipal.getId(), id);
        return ResponseEntity.ok(Map.of("message", "Budget deleted successfully"));
    }
}
