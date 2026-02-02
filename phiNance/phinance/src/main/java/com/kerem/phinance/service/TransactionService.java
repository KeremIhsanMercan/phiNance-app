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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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

    public Page<TransactionDto> getTransactions(String userId, TransactionFilterDto filter) {
        // Sort by date descending, then by updatedAt descending for same-date transactions
        Sort sort = Sort.by(Sort.Order.desc("date"), Sort.Order.desc("updatedAt"));

        // If user specified a different sort, use that instead
        if (filter.getSortBy() != null && !filter.getSortBy().equals("date")) {
            sort = filter.getSortDirection().equalsIgnoreCase("desc")
                    ? Sort.by(filter.getSortBy()).descending()
                    : Sort.by(filter.getSortBy()).ascending();
        }

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        return transactionRepository.findByUserId(userId, pageable)
                .map(this::mapToDto);
    }

    public TransactionDto getTransactionById(String userId, String transactionId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));
        return mapToDto(transaction);
    }

    public TransactionDto createTransaction(String userId, TransactionDto dto) {
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

    public TransactionDto updateTransaction(String userId, String transactionId, TransactionDto dto) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));

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

        Transaction saved = transactionRepository.save(transaction);
        return mapToDto(saved);
    }

    public void deleteTransaction(String userId, String transactionId) {
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
                // Revert goal progress
                goal.setCurrentAmount(goal.getCurrentAmount().subtract(contribution.getAmount()));
                // Unmark as completed if it was completed
                if (goal.isCompleted() && goal.getCurrentAmount().compareTo(goal.getTargetAmount()) < 0) {
                    goal.setCompleted(false);
                }
                goalRepository.save(goal);
            }
            // Delete the contribution record
            goalContributionRepository.delete(contribution);
        }

        transactionRepository.delete(transaction);
    }

    public List<TransactionDto> getTransactionsByDateRange(String userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByUserIdAndDateBetween(userId, startDate, endDate).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
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
        dto.setTransferToAccountId(transaction.getTransferToAccountId());
        dto.setAttachmentUrls(transaction.getAttachmentUrls());
        return dto;
    }
}
