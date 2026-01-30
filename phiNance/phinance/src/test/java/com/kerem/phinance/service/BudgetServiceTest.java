package com.kerem.phinance.service;

import com.kerem.phinance.dto.BudgetDto;
import com.kerem.phinance.model.Budget;
import com.kerem.phinance.model.User;
import com.kerem.phinance.repository.BudgetRepository;
import com.kerem.phinance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BudgetService budgetService;

    private Budget budget;
    private BudgetDto budgetDto;
    private final String userId = "user123";

    @BeforeEach
    void setUp() {
        budget = new Budget();
        budget.setId("budget123");
        budget.setUserId(userId);
        budget.setCategoryId("category123");
        budget.setAllocatedAmount(new BigDecimal("1000.00"));
        budget.setSpentAmount(BigDecimal.ZERO);
        budget.setYear(2024);
        budget.setMonth(1);

        budgetDto = new BudgetDto();
        budgetDto.setCategoryId("category123");
        budgetDto.setAllocatedAmount(new BigDecimal("1000.00"));
        budgetDto.setYear(2024);
        budgetDto.setMonth(1);
    }

    @Test
    void createBudget_Success() {
        when(budgetRepository.findByUserIdAndCategoryIdAndYearAndMonth(
                userId, "category123", 2024, 1)).thenReturn(Optional.empty());
        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);

        BudgetDto result = budgetService.createBudget(userId, budgetDto);

        assertNotNull(result);
        assertEquals(new BigDecimal("1000.00"), result.getAllocatedAmount());
        verify(budgetRepository).save(any(Budget.class));
    }

    @Test
    void updateSpentAmount_Success() {
        when(budgetRepository.findByUserIdAndCategoryIdAndYearAndMonth(
                userId, "category123", 2024, 1)).thenReturn(Optional.of(budget));
        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);

        budgetService.updateSpentAmount(userId, "category123", new BigDecimal("200.00"),
                LocalDate.of(2024, 1, 15));

        assertEquals(new BigDecimal("200.00"), budget.getSpentAmount());
        verify(budgetRepository).save(budget);
    }

    @Test
    void updateSpentAmount_Triggers80PercentAlert() {
        budget.setSpentAmount(new BigDecimal("750.00"));
        User user = new User();
        user.setEmail("test@example.com");

        when(budgetRepository.findByUserIdAndCategoryIdAndYearAndMonth(
                userId, "category123", 2024, 1)).thenReturn(Optional.of(budget));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);

        budgetService.updateSpentAmount(userId, "category123", new BigDecimal("100.00"),
                LocalDate.of(2024, 1, 15));

        assertTrue(budget.isAlertAt80Sent());
    }

    @Test
    void getRemainingAmount_CalculatesCorrectly() {
        budget.setSpentAmount(new BigDecimal("300.00"));

        BigDecimal remaining = budget.getRemainingAmount();

        assertEquals(new BigDecimal("700.00"), remaining);
    }

    @Test
    void getSpentPercentage_CalculatesCorrectly() {
        budget.setSpentAmount(new BigDecimal("250.00"));

        double percentage = budget.getSpentPercentage();

        assertEquals(25.0, percentage, 0.01);
    }
}
