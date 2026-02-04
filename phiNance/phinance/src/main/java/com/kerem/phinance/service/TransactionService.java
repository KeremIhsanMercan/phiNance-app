package com.kerem.phinance.service;

import com.kerem.phinance.dto.TransactionDto;
import com.kerem.phinance.dto.TransactionFilterDto;
import com.kerem.phinance.exception.BadRequestException;
import com.kerem.phinance.exception.ResourceNotFoundException;
import com.kerem.phinance.model.Goal;
import com.kerem.phinance.model.GoalContribution;
import com.kerem.phinance.model.Transaction;
import com.kerem.phinance.repository.GoalContributionRepository;
import com.kerem.phinance.repository.GoalRepository;
import com.kerem.phinance.repository.TransactionRepository;
import com.kerem.phinance.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final BudgetService budgetService;
    private final GoalContributionRepository goalContributionRepository;
    private final GoalRepository goalRepository;

    public Page<TransactionDto> getTransactions(TransactionFilterDto filter) {
        String userId = SecurityUtils.getCurrentUserId();
        // Build sort
        Sort sort;
        if (filter.getSortBy() != null) {
            Sort.Direction direction = filter.getSortDirection() != null
                    && filter.getSortDirection().equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

            // Map frontend sort fields to backend fields
            String sortField = filter.getSortBy();
            switch (sortField) {
                case "account":
                    sortField = "accountId";
                    break;
                case "category":
                    sortField = "categoryId";
                    break;
                // date, amount, type stay the same
            }

            sort = Sort.by(direction, sortField);
        } else {
            // Default sort by date descending
            sort = Sort.by(Sort.Direction.DESC, "date");
        }

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        // Apply filters using repository query
        return transactionRepository.findByFilters(
                userId,
                filter.getStartDate(),
                filter.getEndDate(),
                filter.getAccountId(),
                filter.getCategoryId(),
                filter.getType(),
                filter.getMinAmount(),
                filter.getMaxAmount(),
                filter.getSearchQuery(),
                pageable
        ).map(this::mapToDto);
    }

    public List<TransactionDto> getTransactionsForExport(TransactionFilterDto filter) {
        String userId = SecurityUtils.getCurrentUserId();
        // Build sort (same as getTransactions)
        Sort sort;
        if (filter.getSortBy() != null) {
            Sort.Direction direction = filter.getSortDirection() != null
                    && filter.getSortDirection().equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

            String sortField = filter.getSortBy();
            switch (sortField) {
                case "account":
                    sortField = "accountId";
                    break;
                case "category":
                    sortField = "categoryId";
                    break;
            }

            sort = Sort.by(direction, sortField);
        } else {
            sort = Sort.by(Sort.Direction.DESC, "date");
        }

        // Fetch all matching transactions without pagination
        return transactionRepository.findAllByFilters(
                userId,
                filter.getStartDate(),
                filter.getEndDate(),
                filter.getAccountId(),
                filter.getCategoryId(),
                filter.getType(),
                filter.getMinAmount(),
                filter.getMaxAmount(),
                filter.getSearchQuery(),
                sort
        ).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public TransactionDto getTransactionById(String transactionId) {
        String userId = SecurityUtils.getCurrentUserId();
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));
        return mapToDto(transaction);
    }

    @Transactional
    public TransactionDto createTransaction(TransactionDto dto) {
        String userId = SecurityUtils.getCurrentUserId();
        // Validate account ownership
        if (!accountService.accountBelongsToUser(dto.getAccountId(), userId)) {
            throw new BadRequestException("Account does not belong to user");
        }

        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setAccountId(dto.getAccountId());
        transaction.setType(dto.getType());
        transaction.setAmount(dto.getAmount());
        transaction.setCategoryId(dto.getCategoryId());
        transaction.setDescription(dto.getDescription());
        transaction.setDate(dto.getDate());
        transaction.setRecurring(dto.isRecurring());
        transaction.setRecurrencePattern(dto.getRecurrencePattern());
        transaction.setAutoGenerated(dto.isAutoGenerated());
        transaction.setAttachmentUrls(dto.getAttachmentUrls());

        // Handle transfer
        if (dto.getType() == Transaction.TransactionType.TRANSFER) {
            if (dto.getTransferToAccountId() == null) {
                throw new BadRequestException("Transfer destination account is required");
            }
            if (!accountService.accountBelongsToUser(dto.getTransferToAccountId(), userId)) {
                throw new BadRequestException("Destination account does not belong to user");
            }
            if (dto.getAccountId().equals(dto.getTransferToAccountId())) {
                throw new BadRequestException("Source and destination accounts must be different");
            }
            transaction.setTransferToAccountId(dto.getTransferToAccountId());

            // Update both account balances
            // Deduct from source account
            accountService.updateBalance(dto.getAccountId(), dto.getAmount(), false);
            // Add to destination account
            accountService.updateBalance(dto.getTransferToAccountId(), dto.getAmount(), true);
        } else {
            // Update account balance for non-transfer transactions
            updateAccountBalance(transaction);
        }

        // Update budget if expense
        if (dto.getType() == Transaction.TransactionType.EXPENSE && dto.getCategoryId() != null) {
            budgetService.updateSpentAmount(userId, dto.getCategoryId(), dto.getAmount(), dto.getDate());
        }

        Transaction saved = transactionRepository.save(transaction);
        return mapToDto(saved);
    }

    @Transactional
    public TransactionDto createTransactionWithTransactionSchedular(String userId, TransactionDto dto) {

        // Validate account ownership
        if (!accountService.accountBelongsToUser(dto.getAccountId(), userId)) {
            throw new BadRequestException("Account does not belong to user");
        }

        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setAccountId(dto.getAccountId());
        transaction.setType(dto.getType());
        transaction.setAmount(dto.getAmount());
        transaction.setCategoryId(dto.getCategoryId());
        transaction.setDescription(dto.getDescription());
        transaction.setDate(dto.getDate());
        transaction.setRecurring(dto.isRecurring());
        transaction.setRecurrencePattern(dto.getRecurrencePattern());
        transaction.setAutoGenerated(dto.isAutoGenerated());
        transaction.setAttachmentUrls(dto.getAttachmentUrls());

        // Handle transfer
        if (dto.getType() == Transaction.TransactionType.TRANSFER) {
            if (dto.getTransferToAccountId() == null) {
                throw new BadRequestException("Transfer destination account is required");
            }
            if (!accountService.accountBelongsToUser(dto.getTransferToAccountId(), userId)) {
                throw new BadRequestException("Destination account does not belong to user");
            }
            if (dto.getAccountId().equals(dto.getTransferToAccountId())) {
                throw new BadRequestException("Source and destination accounts must be different");
            }
            transaction.setTransferToAccountId(dto.getTransferToAccountId());

            // Update both account balances
            // Deduct from source account
            accountService.updateBalance(dto.getAccountId(), dto.getAmount(), false);
            // Add to destination account
            accountService.updateBalance(dto.getTransferToAccountId(), dto.getAmount(), true);
        } else {
            // Update account balance for non-transfer transactions
            updateAccountBalance(transaction);
        }

        // Update budget if expense
        if (dto.getType() == Transaction.TransactionType.EXPENSE && dto.getCategoryId() != null) {
            budgetService.updateSpentAmount(userId, dto.getCategoryId(), dto.getAmount(), dto.getDate());
        }

        Transaction saved = transactionRepository.save(transaction);
        return mapToDto(saved);
    }

    @Transactional
    public TransactionDto updateTransaction(String transactionId, TransactionDto dto) {
        String userId = SecurityUtils.getCurrentUserId();
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));

        // Prevent editing auto-generated transactions
        if (transaction.isAutoGenerated()) {
            throw new BadRequestException("Cannot edit auto-generated recurring transactions");
        }

        // If transaction is recurring and it is old
        if (transaction.isRecurring() && transaction.getDate().isBefore(LocalDate.now().minusMonths(1))) {
            // unless it is only changing the recurrence pattern from non-recurring to recurring or vice versa, we should not allow editing old recurring transactions
            if (!(transaction.isRecurring() != dto.isRecurring())) {
                throw new BadRequestException("Cannot edit recurring transactions that have recurrences");
            }
        }

        // Store old amount for goal contribution update
        BigDecimal oldAmount = transaction.getAmount();

        // Reverse the previous balance change
        if (transaction.getType() == Transaction.TransactionType.TRANSFER) {
            // Reverse old transfer
            accountService.updateBalance(transaction.getAccountId(), transaction.getAmount(), true);
            accountService.updateBalance(transaction.getTransferToAccountId(), transaction.getAmount(), false);
        } else {
            reverseAccountBalance(transaction);
        }

        // Update transaction
        transaction.setAmount(dto.getAmount());
        transaction.setCategoryId(dto.getCategoryId());
        transaction.setDescription(dto.getDescription());
        transaction.setDate(dto.getDate());
        transaction.setRecurring(dto.isRecurring());
        transaction.setRecurrencePattern(dto.getRecurrencePattern());
        transaction.setAttachmentUrls(dto.getAttachmentUrls());

        // Apply new balance change
        if (transaction.getType() == Transaction.TransactionType.TRANSFER) {
            // Apply new transfer
            accountService.updateBalance(transaction.getAccountId(), dto.getAmount(), false);
            accountService.updateBalance(transaction.getTransferToAccountId(), dto.getAmount(), true);
        } else {
            updateAccountBalance(transaction);
        }

        // Update goal contribution if this is a goal contribution transaction
        GoalContribution contribution = goalContributionRepository.findByTransactionId(transactionId);
        if (contribution != null) {
            Goal goal = goalRepository.findById(contribution.getGoalId()).orElse(null);
            if (goal != null) {
                boolean wasCompleted = goal.isCompleted();

                // Revert old contribution amount
                goal.setCurrentAmount(goal.getCurrentAmount().subtract(oldAmount));
                // Add new contribution amount
                goal.setCurrentAmount(goal.getCurrentAmount().add(dto.getAmount()));

                // Update completion status
                if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
                    // Check if all dependencies are completed
                    boolean canComplete = true;
                    if (goal.getDependencyGoalIds() != null && !goal.getDependencyGoalIds().isEmpty()) {
                        for (String dependencyId : goal.getDependencyGoalIds()) {
                            Goal depGoal = goalRepository.findById(dependencyId).orElse(null);
                            if (depGoal == null || !depGoal.isCompleted()) {
                                canComplete = false;
                                break;
                            }
                        }
                    }
                    if (canComplete) {
                        goal.setCompleted(true);
                    } else {
                        goal.setCompleted(false);
                    }
                } else {
                    goal.setCompleted(false);
                }
                goalRepository.save(goal);

                // If goal was completed but now is not, mark dependent goals as incomplete
                if (wasCompleted && !goal.isCompleted()) {
                    markDependentGoalsIncomplete(goal.getId());
                }

                // Update the contribution record amount
                contribution.setAmount(dto.getAmount());
                goalContributionRepository.save(contribution);
            }
        }

        Transaction saved = transactionRepository.save(transaction);
        return mapToDto(saved);
    }

    @Transactional
    public void deleteTransaction(String transactionId) {
        String userId = SecurityUtils.getCurrentUserId();
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));

        // Reverse the balance changes
        if (transaction.getType() == Transaction.TransactionType.TRANSFER) {
            // Reverse transfer: add back to source, deduct from destination
            accountService.updateBalance(transaction.getAccountId(), transaction.getAmount(), true);
            accountService.updateBalance(transaction.getTransferToAccountId(), transaction.getAmount(), false);
        } else {
            reverseAccountBalance(transaction);
        }

        // Subtract from budget if expense
        if (transaction.getType() == Transaction.TransactionType.EXPENSE && transaction.getCategoryId() != null) {
            budgetService.updateSpentAmount(userId, transaction.getCategoryId(), transaction.getAmount().negate(), transaction.getDate());
        }

        // Revert goal contribution if this is a goal contribution transaction
        GoalContribution contribution = goalContributionRepository.findByTransactionId(transactionId);
        if (contribution != null) {
            Goal goal = goalRepository.findById(contribution.getGoalId()).orElse(null);
            if (goal != null) {
                boolean wasCompleted = goal.isCompleted();

                // Revert goal progress
                goal.setCurrentAmount(goal.getCurrentAmount().subtract(contribution.getAmount()));

                // Unmark as completed if it was completed
                if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) < 0) {
                    goal.setCompleted(false);
                }
                goalRepository.save(goal);

                // If goal was completed but now is not, mark dependent goals as incomplete
                if (wasCompleted && !goal.isCompleted()) {
                    markDependentGoalsIncomplete(goal.getId());
                }
            }
            // Delete the contribution record
            goalContributionRepository.delete(contribution);
        }

        transactionRepository.delete(transaction);
    }

    public List<TransactionDto> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        String userId = SecurityUtils.getCurrentUserId();
        return transactionRepository.findByUserIdAndDateBetween(userId, startDate, endDate).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private void markDependentGoalsIncomplete(String goalId) {
        // Find all goals that depend on this goal
        List<Goal> dependentGoals = goalRepository.findByDependencyGoalIdsContaining(goalId);

        for (Goal dependentGoal : dependentGoals) {
            if (dependentGoal.isCompleted()) {
                dependentGoal.setCompleted(false);
                goalRepository.save(dependentGoal);

                // Recursively mark goals that depend on this dependent goal
                markDependentGoalsIncomplete(dependentGoal.getId());
            }
        }
    }

    private void updateAccountBalance(Transaction transaction) {
        boolean isAddition = transaction.getType() == Transaction.TransactionType.INCOME;
        accountService.updateBalance(transaction.getAccountId(), transaction.getAmount(), isAddition);
    }

    private void reverseAccountBalance(Transaction transaction) {
        boolean isAddition = transaction.getType() != Transaction.TransactionType.INCOME;
        accountService.updateBalance(transaction.getAccountId(), transaction.getAmount(), isAddition);
    }

    private TransactionDto mapToDto(Transaction transaction) {
        TransactionDto dto = new TransactionDto();
        dto.setId(transaction.getId());
        dto.setAccountId(transaction.getAccountId());
        dto.setType(transaction.getType());
        dto.setAmount(transaction.getAmount());
        dto.setCategoryId(transaction.getCategoryId());
        dto.setDescription(transaction.getDescription());
        dto.setDate(transaction.getDate());
        dto.setRecurring(transaction.isRecurring());
        dto.setRecurrencePattern(transaction.getRecurrencePattern());
        dto.setAutoGenerated(transaction.isAutoGenerated());
        dto.setTransferToAccountId(transaction.getTransferToAccountId());
        dto.setAttachmentUrls(transaction.getAttachmentUrls());
        return dto;
    }
}
