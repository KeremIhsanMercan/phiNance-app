package com.kerem.phinance.service;

import com.kerem.phinance.dto.GoalContributionDto;
import com.kerem.phinance.dto.GoalDto;
import com.kerem.phinance.dto.TransactionDto;
import com.kerem.phinance.exception.BadRequestException;
import com.kerem.phinance.model.Goal;
import com.kerem.phinance.model.GoalContribution;
import com.kerem.phinance.repository.GoalContributionRepository;
import com.kerem.phinance.repository.GoalRepository;
import com.kerem.phinance.security.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private GoalContributionRepository contributionRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private GoalService goalService;

    private Goal goal;
    private Goal dependencyGoal;
    private GoalDto goalDto;
    private final String userId = "user123";
    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(userId);
        goal = new Goal();
        goal.setId("goal123");
        goal.setUserId(userId);
        goal.setName("Emergency Fund");
        goal.setTargetAmount(new BigDecimal("5000.00"));
        goal.setCurrentAmount(BigDecimal.ZERO);
        goal.setDeadline(LocalDate.now().plusMonths(12));
        goal.setPriority(Goal.Priority.HIGH);
        goal.setDependencyGoalIds(new ArrayList<>());

        dependencyGoal = new Goal();
        dependencyGoal.setId("dependency123");
        dependencyGoal.setUserId(userId);
        dependencyGoal.setName("Pay off debt");
        dependencyGoal.setCompleted(false);

        goalDto = new GoalDto();
        goalDto.setName("Emergency Fund");
        goalDto.setTargetAmount(new BigDecimal("5000.00"));
        goalDto.setDeadline(LocalDate.now().plusMonths(12));
        goalDto.setPriority(Goal.Priority.HIGH);
    }

    @Test
    void validateGoalDependencies_NoDependencies_ReturnsTrue() {
        when(goalRepository.findByIdAndUserId("goal123", userId)).thenReturn(Optional.of(goal));

        boolean result = goalService.validateGoalDependencies("goal123");

        assertTrue(result);
    }

    @Test
    void validateGoalDependencies_DependencyNotCompleted_ReturnsFalse() {
        goal.setDependencyGoalIds(Arrays.asList("dependency123"));

        when(goalRepository.findByIdAndUserId("goal123", userId)).thenReturn(Optional.of(goal));
        when(goalRepository.findById("dependency123")).thenReturn(Optional.of(dependencyGoal));

        boolean result = goalService.validateGoalDependencies("goal123");

        assertFalse(result);
    }

    @Test
    void validateGoalDependencies_DependencyCompleted_ReturnsTrue() {
        goal.setDependencyGoalIds(Arrays.asList("dependency123"));
        dependencyGoal.setCompleted(true);

        when(goalRepository.findByIdAndUserId("goal123", userId)).thenReturn(Optional.of(goal));
        when(goalRepository.findById("dependency123")).thenReturn(Optional.of(dependencyGoal));

        boolean result = goalService.validateGoalDependencies("goal123");

        assertTrue(result);
    }

    @Test
    void markAsCompleted_WithUncompletedDependency_ThrowsException() {
        goal.setDependencyGoalIds(Arrays.asList("dependency123"));

        when(goalRepository.findByIdAndUserId("goal123", userId)).thenReturn(Optional.of(goal));
        when(goalRepository.findById("dependency123")).thenReturn(Optional.of(dependencyGoal));

        assertThrows(BadRequestException.class,
                () -> goalService.markAsCompleted("goal123"));
    }

    @Test
    void markAsCompleted_WithCompletedDependency_Success() {
        goal.setDependencyGoalIds(Arrays.asList("dependency123"));
        dependencyGoal.setCompleted(true);

        when(goalRepository.findByIdAndUserId("goal123", userId)).thenReturn(Optional.of(goal));
        when(goalRepository.findById("dependency123")).thenReturn(Optional.of(dependencyGoal));
        when(goalRepository.save(any(Goal.class))).thenReturn(goal);

        GoalDto result = goalService.markAsCompleted("goal123");

        assertNotNull(result);
        assertTrue(goal.isCompleted());
    }

    @Test
    void deleteGoal_WithDependentGoals_ThrowsException() {
        Goal dependentGoal = new Goal();
        dependentGoal.setId("dependent123");
        dependentGoal.setDependencyGoalIds(Arrays.asList("goal123"));

        when(goalRepository.findByIdAndUserId("goal123", userId)).thenReturn(Optional.of(goal));
        when(goalRepository.findByDependencyGoalIdsContaining("goal123"))
                .thenReturn(Collections.singletonList(dependentGoal));

        assertThrows(BadRequestException.class,
                () -> goalService.deleteGoal("goal123"));
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    void addContribution_CompletesGoalWhenTargetReached() {
        goal.setCurrentAmount(new BigDecimal("4500.00"));
        goal.setSavingsAccountId("savings123");

        GoalContributionDto contributionDto = new GoalContributionDto();
        contributionDto.setGoalId("goal123");
        contributionDto.setAccountId("account123");
        contributionDto.setAmount(new BigDecimal("500.00"));

        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setId("trans123");

        when(goalRepository.findByIdAndUserId("goal123", userId)).thenReturn(Optional.of(goal));
        when(contributionRepository.save(any(GoalContribution.class)))
                .thenReturn(new GoalContribution());
        when(goalRepository.save(any(Goal.class))).thenReturn(goal);
        when(transactionService.createTransaction(any())).thenReturn(transactionDto);

        GoalDto result = goalService.addContribution(contributionDto);

        assertTrue(goal.isCompleted());
        assertEquals(new BigDecimal("5000.00"), goal.getCurrentAmount());
    }
}
