package com.kerem.phinance.service;

import com.kerem.phinance.dto.TransactionDto;
import com.kerem.phinance.dto.TransactionFilterDto;
import com.kerem.phinance.exception.BadRequestException;
import com.kerem.phinance.exception.ResourceNotFoundException;
import com.kerem.phinance.model.Transaction;
import com.kerem.phinance.repository.GoalContributionRepository;
import com.kerem.phinance.repository.GoalRepository;
import com.kerem.phinance.repository.TransactionRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private BudgetService budgetService;

    @Mock
    private GoalContributionRepository goalContributionRepository;

    @Mock
    private GoalRepository goalRepository;

    @InjectMocks
    private TransactionService transactionService;

    private TransactionDto transactionDto;
    private Transaction transaction;
    private final String userId = "user123";
    private final String accountId = "account123";
    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(userId);
        transactionDto = new TransactionDto();
        transactionDto.setAccountId(accountId);
        transactionDto.setType(Transaction.TransactionType.EXPENSE);
        transactionDto.setAmount(new BigDecimal("100.00"));
        transactionDto.setDescription("Test transaction");
        transactionDto.setDate(LocalDate.now());
        transactionDto.setCategoryId("category123");

        transaction = new Transaction();
        transaction.setId("transaction123");
        transaction.setUserId(userId);
        transaction.setAccountId(accountId);
        transaction.setType(Transaction.TransactionType.EXPENSE);
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setDescription("Test transaction");
        transaction.setDate(LocalDate.now());
    }

    @Test
    void createTransaction_Success() {
        when(accountService.accountBelongsToUser(accountId, userId)).thenReturn(true);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        doNothing().when(accountService).updateBalance(anyString(), any(BigDecimal.class), anyBoolean());
        doNothing().when(budgetService).updateSpentAmount(anyString(), anyString(), any(BigDecimal.class), any(LocalDate.class));

        TransactionDto result = transactionService.createTransaction(transactionDto);

        assertNotNull(result);
        assertEquals(new BigDecimal("100.00"), result.getAmount());
        assertEquals(Transaction.TransactionType.EXPENSE, result.getType());
        verify(accountService).updateBalance(eq(accountId), eq(new BigDecimal("100.00")), eq(false));
        verify(budgetService).updateSpentAmount(eq(userId), eq("category123"), any(BigDecimal.class), any(LocalDate.class));
    }

    @Test
    void createTransaction_AccountNotOwned_ThrowsException() {
        when(accountService.accountBelongsToUser(accountId, userId)).thenReturn(false);

        assertThrows(BadRequestException.class,
                () -> transactionService.createTransaction(transactionDto));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void createTransaction_IncomeUpdatesBalanceCorrectly() {
        transactionDto.setType(Transaction.TransactionType.INCOME);
        transactionDto.setCategoryId(null);
        transaction.setType(Transaction.TransactionType.INCOME);

        when(accountService.accountBelongsToUser(accountId, userId)).thenReturn(true);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        doNothing().when(accountService).updateBalance(anyString(), any(BigDecimal.class), anyBoolean());

        TransactionDto result = transactionService.createTransaction(transactionDto);

        assertNotNull(result);
        verify(accountService).updateBalance(eq(accountId), eq(new BigDecimal("100.00")), eq(true));
    }

    @Test
    void deleteTransaction_Success() {
        when(transactionRepository.findByIdAndUserId("transaction123", userId))
                .thenReturn(Optional.of(transaction));
        when(goalContributionRepository.findByTransactionId("transaction123")).thenReturn(null);
        doNothing().when(accountService).updateBalance(anyString(), any(BigDecimal.class), anyBoolean());

        transactionService.deleteTransaction("transaction123");

        verify(transactionRepository).delete(transaction);
        verify(accountService).updateBalance(eq(accountId), eq(new BigDecimal("100.00")), eq(true));
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }
}
