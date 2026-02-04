package com.kerem.phinance.controller;

import com.kerem.phinance.dto.BudgetDto;
import com.kerem.phinance.service.BudgetService;
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
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@Tag(name = "Budgets", description = "Budget management APIs")
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    @Operation(summary = "Get budgets by month")
    public ResponseEntity<Page<BudgetDto>> getBudgetsByMonth(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "allocatedAmount") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        // If year and month are provided, filter by them
        if (year != null && month != null) {
            return ResponseEntity.ok(budgetService.getBudgetsByMonthPaginated(year, month, pageable));
        }

        // Otherwise, return all budgets
        return ResponseEntity.ok(budgetService.getAllBudgetsPaginated(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get budget by ID")
    public ResponseEntity<BudgetDto> getBudgetById(
            @PathVariable String id) {
        return ResponseEntity.ok(budgetService.getBudgetById(id));
    }

    @GetMapping("/compare")
    @Operation(summary = "Compare budgets between two months")
    public ResponseEntity<List<BudgetDto>> compareBudgets(
            @RequestParam int year1,
            @RequestParam int month1,
            @RequestParam int year2,
            @RequestParam int month2) {
        return ResponseEntity.ok(budgetService.compareBudgets(
                year1, month1, year2, month2));
    }

    @PostMapping
    @Operation(summary = "Create a new budget")
    public ResponseEntity<BudgetDto> createBudget(
            @Valid @RequestBody BudgetDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(budgetService.createBudget(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a budget")
    public ResponseEntity<BudgetDto> updateBudget(
            @PathVariable String id,
            @Valid @RequestBody BudgetDto dto) {
        return ResponseEntity.ok(budgetService.updateBudget(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a budget")
    public ResponseEntity<Map<String, String>> deleteBudget(
            @PathVariable String id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.ok(Map.of("message", "Budget deleted successfully"));
    }
}
