package com.kerem.phinance.service;

import com.kerem.phinance.dto.GoalContributionDto;
import com.kerem.phinance.dto.GoalDto;
import com.kerem.phinance.dto.TransactionDto;
import com.kerem.phinance.exception.BadRequestException;
import com.kerem.phinance.exception.ResourceNotFoundException;
import com.kerem.phinance.model.Account;
import com.kerem.phinance.model.Goal;
import com.kerem.phinance.model.GoalContribution;
import com.kerem.phinance.model.Transaction;
import com.kerem.phinance.repository.AccountRepository;
import com.kerem.phinance.repository.GoalContributionRepository;
import com.kerem.phinance.repository.GoalRepository;
import com.kerem.phinance.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final GoalContributionRepository contributionRepository;
    private final AccountRepository accountRepository;
    private final TransactionService transactionService;
    private final AccountService accountService;

    public Page<GoalDto> getGoalsPaginated(Pageable pageable) {
        String userId = SecurityUtils.getCurrentUserId();
        return goalRepository.findByUserId(userId, pageable)
                .map(this::mapToDto);
    }

    public List<GoalDto> getActiveGoals() {
        String userId = SecurityUtils.getCurrentUserId();
        return goalRepository.findByUserIdAndCompletedFalse(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public GoalDto getGoalById(String goalId) {
        String userId = SecurityUtils.getCurrentUserId();
        Goal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", goalId));
        return mapToDto(goal);
    }

    public GoalDto createGoal(GoalDto dto) {
        String userId = SecurityUtils.getCurrentUserId();
        // Create a SAVINGS account for this goal
        Account savingsAccount = new Account();
        savingsAccount.setUserId(userId);
        savingsAccount.setName(dto.getName());
        savingsAccount.setType(Account.AccountType.SAVINGS);
        savingsAccount.setInitialBalance(BigDecimal.ZERO);
        savingsAccount.setCurrentBalance(BigDecimal.ZERO);
        savingsAccount.setCurrency("USD");
        savingsAccount.setColor(dto.getColor() != null ? dto.getColor() : "#3B82F6");
        savingsAccount.setIcon(dto.getIcon());
        savingsAccount.setDescription("Savings account for goal: " + dto.getName());

        Account savedAccount = accountRepository.save(savingsAccount);

        // Create the goal with the savings account ID
        Goal goal = new Goal();
        goal.setUserId(userId);
        goal.setName(dto.getName());
        goal.setDescription(dto.getDescription());
        goal.setTargetAmount(dto.getTargetAmount());
        goal.setDeadline(dto.getDeadline());
        goal.setPriority(dto.getPriority());
        goal.setAccountId(dto.getAccountId());
        goal.setDependencyGoalIds(dto.getDependencyGoalIds());
        goal.setSavingsAccountId(savedAccount.getId());
        goal.setColor(dto.getColor() != null ? dto.getColor() : "#3B82F6");
        goal.setIcon(dto.getIcon());

        Goal saved = goalRepository.save(goal);
        return mapToDto(saved);
    }

    public GoalDto updateGoal(String goalId, GoalDto dto) {
        String userId = SecurityUtils.getCurrentUserId();
        Goal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", goalId));

        // Update savings account if name or color changed
        if (goal.getSavingsAccountId() != null) {
            Account account = accountRepository.findById(goal.getSavingsAccountId()).orElse(null);
            if (account != null) {
                boolean accountUpdated = false;
                if (!goal.getName().equals(dto.getName())) {
                    account.setName(dto.getName());
                    accountUpdated = true;
                }
                if (dto.getColor() != null && !dto.getColor().equals(account.getColor())) {
                    account.setColor(dto.getColor());
                    accountUpdated = true;
                }
                if (accountUpdated) {
                    accountRepository.save(account);
                }
            }
        }

        goal.setName(dto.getName());
        goal.setDescription(dto.getDescription());
        goal.setTargetAmount(dto.getTargetAmount());
        goal.setDeadline(dto.getDeadline());
        goal.setPriority(dto.getPriority());
        goal.setAccountId(dto.getAccountId());
        goal.setDependencyGoalIds(dto.getDependencyGoalIds());
        if (dto.getColor() != null) {
            goal.setColor(dto.getColor());
        }
        goal.setIcon(dto.getIcon());

        Goal saved = goalRepository.save(goal);
        return mapToDto(saved);
    }

    public void deleteGoal(String goalId) {
        String userId = SecurityUtils.getCurrentUserId();
        Goal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", goalId));

        // Check if any goals depend on this one
        List<Goal> dependentGoals = goalRepository.findByDependencyGoalIdsContaining(goalId);
        if (!dependentGoals.isEmpty()) {
            throw new BadRequestException("Cannot delete goal with dependent goals");
        }

        // Archive the associated savings account
        if (goal.getSavingsAccountId() != null) {
            accountService.archiveAccount(goal.getSavingsAccountId());
        }

        goalRepository.delete(goal);
    }

    public GoalDto addContribution(GoalContributionDto dto) {
        String userId = SecurityUtils.getCurrentUserId();
        Goal goal = goalRepository.findByIdAndUserId(dto.getGoalId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", dto.getGoalId()));

        // Create TRANSFER transaction to the savings account
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        String description = today.format(formatter) + " " + goal.getName() + " Contribution";

        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setAccountId(dto.getAccountId());
        transactionDto.setType(Transaction.TransactionType.TRANSFER);
        transactionDto.setAmount(dto.getAmount());
        transactionDto.setTransferToAccountId(goal.getSavingsAccountId());
        transactionDto.setDescription(description);
        transactionDto.setDate(today);
        transactionDto.setRecurring(false);

        TransactionDto createdTransaction = transactionService.createTransaction(transactionDto);

        // Create contribution record with transaction ID
        GoalContribution contribution = new GoalContribution();
        contribution.setGoalId(dto.getGoalId());
        contribution.setUserId(userId);
        contribution.setAmount(dto.getAmount());
        contribution.setNote(dto.getNote());
        contribution.setTransactionId(createdTransaction.getId());
        contributionRepository.save(contribution);

        // Update goal current amount
        goal.setCurrentAmount(goal.getCurrentAmount().add(dto.getAmount()));

        // Check if goal reached target and all dependencies are completed
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            // Only mark as completed if all dependencies are met
            boolean canComplete = true;
            if (goal.getDependencyGoalIds() != null && !goal.getDependencyGoalIds().isEmpty()) {
                for (String dependencyId : goal.getDependencyGoalIds()) {
                    Goal dependency = goalRepository.findById(dependencyId).orElse(null);
                    if (dependency == null || !dependency.isCompleted()) {
                        canComplete = false;
                        break;
                    }
                }
            }

            if (canComplete) {
                goal.setCompleted(true);
            }
        }

        Goal saved = goalRepository.save(goal);
        return mapToDto(saved);
    }

    public GoalDto markAsCompleted(String goalId) {
        String userId = SecurityUtils.getCurrentUserId();
        Goal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", goalId));

        // Check if dependencies are completed
        if (goal.getDependencyGoalIds() != null && !goal.getDependencyGoalIds().isEmpty()) {
            for (String dependencyId : goal.getDependencyGoalIds()) {
                Goal dependency = goalRepository.findById(dependencyId)
                        .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", dependencyId));
                if (!dependency.isCompleted()) {
                    throw new BadRequestException("Cannot complete goal: dependency '" + dependency.getName() + "' is not completed");
                }
            }
        }

        goal.setCompleted(true);
        Goal saved = goalRepository.save(goal);
        return mapToDto(saved);
    }

    public boolean validateGoalDependencies(String goalId) {
        String userId = SecurityUtils.getCurrentUserId();
        Goal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", goalId));

        if (goal.getDependencyGoalIds() == null || goal.getDependencyGoalIds().isEmpty()) {
            return true;
        }

        for (String dependencyId : goal.getDependencyGoalIds()) {
            Goal dependency = goalRepository.findById(dependencyId).orElse(null);
            if (dependency == null || !dependency.isCompleted()) {
                return false;
            }
        }

        return true;
    }

    public GoalDto addDependency(String goalId, String dependencyGoalId) {
        String userId = SecurityUtils.getCurrentUserId();
        Goal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", goalId));

        Goal dependencyGoal = goalRepository.findByIdAndUserId(dependencyGoalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", dependencyGoalId));

        // Check if dependency already exists
        if (goal.getDependencyGoalIds() != null && goal.getDependencyGoalIds().contains(dependencyGoalId)) {
            throw new BadRequestException("Dependency already exists");
        }

        // Check for circular dependency
        if (wouldCreateCircularDependency(goalId, dependencyGoalId)) {
            throw new BadRequestException("Cannot add dependency: this would create a circular dependency");
        }

        if (goal.getDependencyGoalIds() == null) {
            goal.setDependencyGoalIds(new ArrayList<>());
        }
        goal.getDependencyGoalIds().add(dependencyGoalId);

        Goal saved = goalRepository.save(goal);
        return mapToDto(saved);
    }

    public GoalDto removeDependency(String goalId, String dependencyGoalId) {
        String userId = SecurityUtils.getCurrentUserId();
        Goal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", goalId));

        if (goal.getDependencyGoalIds() != null) {
            goal.getDependencyGoalIds().remove(dependencyGoalId);
        }

        Goal saved = goalRepository.save(goal);
        return mapToDto(saved);
    }

    private boolean wouldCreateCircularDependency(String goalId, String dependencyGoalId) {
        // Check if dependencyGoal depends on goalId (directly or indirectly)
        return checkDependencyChain(dependencyGoalId, goalId);
    }

    private boolean checkDependencyChain(String currentGoalId, String targetGoalId) {
        if (currentGoalId.equals(targetGoalId)) {
            return true; // Found circular dependency
        }

        Goal currentGoal = goalRepository.findById(currentGoalId).orElse(null);
        if (currentGoal == null || currentGoal.getDependencyGoalIds() == null) {
            return false;
        }

        // Recursively check all dependencies
        for (String depId : currentGoal.getDependencyGoalIds()) {
            if (checkDependencyChain(depId, targetGoalId)) {
                return true;
            }
        }

        return false;
    }

    private GoalDto mapToDto(Goal goal) {
        GoalDto dto = new GoalDto();
        dto.setId(goal.getId());
        dto.setName(goal.getName());
        dto.setDescription(goal.getDescription());
        dto.setTargetAmount(goal.getTargetAmount());
        dto.setCurrentAmount(goal.getCurrentAmount());
        dto.setDeadline(goal.getDeadline());
        dto.setPriority(goal.getPriority());
        dto.setAccountId(goal.getAccountId());
        dto.setDependencyGoalIds(goal.getDependencyGoalIds());
        dto.setCompleted(goal.isCompleted());
        dto.setSavingsAccountId(goal.getSavingsAccountId());
        dto.setColor(goal.getColor());
        dto.setIcon(goal.getIcon());
        dto.setProgressPercentage(goal.getProgressPercentage());
        return dto;
    }
}
