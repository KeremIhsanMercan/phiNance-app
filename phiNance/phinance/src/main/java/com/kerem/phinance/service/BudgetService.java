package com.kerem.phinance.service;

import com.kerem.phinance.dto.BudgetDto;
import com.kerem.phinance.exception.ResourceNotFoundException;
import com.kerem.phinance.model.Budget;
import com.kerem.phinance.model.User;
import com.kerem.phinance.repository.BudgetRepository;
import com.kerem.phinance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;

    public List<BudgetDto> getBudgetsByMonth(String userId, int year, int month) {
        return budgetRepository.findByUserIdAndYearAndMonth(userId, year, month).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public BudgetDto getBudgetById(String userId, String budgetId) {
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", budgetId));
        return mapToDto(budget);
    }

    public BudgetDto createBudget(String userId, BudgetDto dto) {
        // Check if budget already exists for this category and period
        Optional<Budget> existing = budgetRepository.findByUserIdAndCategoryIdAndYearAndMonth(
                userId, dto.getCategoryId(), dto.getYear(), dto.getMonth());

        if (existing.isPresent()) {
            return updateBudget(userId, existing.get().getId(), dto);
        }

        Budget budget = new Budget();
        budget.setUserId(userId);
        budget.setCategoryId(dto.getCategoryId());
        budget.setAllocatedAmount(dto.getAllocatedAmount());
        budget.setYear(dto.getYear());
        budget.setMonth(dto.getMonth());

        Budget saved = budgetRepository.save(budget);
        return mapToDto(saved);
    }

    public BudgetDto updateBudget(String userId, String budgetId, BudgetDto dto) {
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", budgetId));

        budget.setAllocatedAmount(dto.getAllocatedAmount());

        Budget saved = budgetRepository.save(budget);
        return mapToDto(saved);
    }

    public void deleteBudget(String userId, String budgetId) {
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", budgetId));

        budgetRepository.delete(budget);
    }

    public void updateSpentAmount(String userId, String categoryId, BigDecimal amount, LocalDate date) {
        Optional<Budget> budgetOpt = budgetRepository.findByUserIdAndCategoryIdAndYearAndMonth(
                userId, categoryId, date.getYear(), date.getMonthValue());

        if (budgetOpt.isPresent()) {
            Budget budget = budgetOpt.get();
            budget.setSpentAmount(budget.getSpentAmount().add(amount));

            // Check for budget alerts (notifications disabled) - update flags only
            checkBudgetAlerts(budget);

            budgetRepository.save(budget);
        }
    }

    private void checkBudgetAlerts(Budget budget) {
        double percentage = budget.getSpentPercentage();

        if (percentage >= 100 && !budget.isAlertAt100Sent()) {
            // Notifications are disabled; just mark the alert as sent
            budget.setAlertAt100Sent(true);
        } else if (percentage >= 80 && !budget.isAlertAt80Sent()) {
            // Notifications are disabled; just mark the alert as sent
            budget.setAlertAt80Sent(true);
        }
    }

    public List<BudgetDto> compareBudgets(String userId, int year1, int month1, int year2, int month2) {
        List<Budget> budgets1 = budgetRepository.findByUserIdAndYearAndMonth(userId, year1, month1);
        List<Budget> budgets2 = budgetRepository.findByUserIdAndYearAndMonth(userId, year2, month2);

        // Combine both lists for comparison
        budgets1.addAll(budgets2);

        return budgets1.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private BudgetDto mapToDto(Budget budget) {
        BudgetDto dto = new BudgetDto();
        dto.setId(budget.getId());
        dto.setCategoryId(budget.getCategoryId());
        dto.setAllocatedAmount(budget.getAllocatedAmount());
        dto.setSpentAmount(budget.getSpentAmount());
        dto.setRemainingAmount(budget.getRemainingAmount());
        dto.setYear(budget.getYear());
        dto.setMonth(budget.getMonth());
        dto.setSpentPercentage(budget.getSpentPercentage());
        return dto;
    }
}
