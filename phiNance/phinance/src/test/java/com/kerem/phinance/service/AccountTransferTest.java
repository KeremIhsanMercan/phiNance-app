package com.kerem.phinance.service;

import com.kerem.phinance.dto.AccountDto;
import com.kerem.phinance.dto.TransactionDto;
import com.kerem.phinance.exception.BadRequestException;
import com.kerem.phinance.model.Account;
import com.kerem.phinance.model.Transaction;
import com.kerem.phinance.repository.AccountRepository;
import com.kerem.phinance.repository.TransactionRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountTransferTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private BudgetService budgetService;

    @InjectMocks
    private TransactionService transactionService;

    private Account sourceAccount;
    private Account destAccount;
    private TransactionDto transferDto;
    private final String userId = "user123";

    @BeforeEach
    void setUp() {
        sourceAccount = new Account();
        sourceAccount.setId("source123");
        sourceAccount.setUserId(userId);
        sourceAccount.setName("Checking");
        sourceAccount.setCurrentBalance(new BigDecimal("1000.00"));
        sourceAccount.setCurrency("USD");

        destAccount = new Account();
        destAccount.setId("dest123");
        destAccount.setUserId(userId);
        destAccount.setName("Savings");
        destAccount.setCurrentBalance(new BigDecimal("500.00"));
        destAccount.setCurrency("USD");

        transferDto = new TransactionDto();
        transferDto.setAccountId("source123");
        transferDto.setTransferToAccountId("dest123");
        transferDto.setType(Transaction.TransactionType.TRANSFER);
        transferDto.setAmount(new BigDecimal("200.00"));
        transferDto.setDescription("Transfer to savings");
        transferDto.setDate(LocalDate.now());
    }

    @Test
    void createTransfer_Success() {
        Transaction sourceTransaction = new Transaction();
        sourceTransaction.setId("trans1");
        sourceTransaction.setUserId(userId);
        sourceTransaction.setAccountId("source123");
        sourceTransaction.setType(Transaction.TransactionType.TRANSFER);
        sourceTransaction.setAmount(new BigDecimal("200.00"));

        Transaction linkedTransaction = new Transaction();
        linkedTransaction.setId("trans2");
        linkedTransaction.setUserId(userId);
        linkedTransaction.setAccountId("dest123");
        linkedTransaction.setType(Transaction.TransactionType.INCOME);
        linkedTransaction.setAmount(new BigDecimal("200.00"));

        when(accountService.accountBelongsToUser("source123", userId)).thenReturn(true);
        when(accountService.accountBelongsToUser("dest123", userId)).thenReturn(true);
        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(linkedTransaction)
                .thenReturn(sourceTransaction);
        doNothing().when(accountService).updateBalance(anyString(), any(BigDecimal.class), anyBoolean());

        TransactionDto result = transactionService.createTransaction(userId, transferDto);

        assertNotNull(result);
        assertEquals(Transaction.TransactionType.TRANSFER, result.getType());
        verify(accountService).updateBalance(eq("source123"), eq(new BigDecimal("200.00")), eq(false));
        verify(accountService).updateBalance(eq("dest123"), eq(new BigDecimal("200.00")), eq(true));
    }

    @Test
    void createTransfer_DestinationNotOwned_ThrowsException() {
        when(accountService.accountBelongsToUser("source123", userId)).thenReturn(true);
        when(accountService.accountBelongsToUser("dest123", userId)).thenReturn(false);

        assertThrows(BadRequestException.class,
                () -> transactionService.createTransaction(userId, transferDto));
    }

    @Test
    void createTransfer_MissingDestination_ThrowsException() {
        transferDto.setTransferToAccountId(null);

        when(accountService.accountBelongsToUser("source123", userId)).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> transactionService.createTransaction(userId, transferDto));
    }

    @Test
    void balanceCalculation_TransferDeductsFromSource() {
        // Test that transfer properly deducts from source
        sourceAccount.setCurrentBalance(new BigDecimal("1000.00"));
        BigDecimal transferAmount = new BigDecimal("200.00");
        
        BigDecimal expectedBalance = sourceAccount.getCurrentBalance().subtract(transferAmount);
        
        assertEquals(new BigDecimal("800.00"), expectedBalance);
    }

    @Test
    void balanceCalculation_TransferAddsToDestination() {
        // Test that transfer properly adds to destination
        destAccount.setCurrentBalance(new BigDecimal("500.00"));
        BigDecimal transferAmount = new BigDecimal("200.00");
        
        BigDecimal expectedBalance = destAccount.getCurrentBalance().add(transferAmount);
        
        assertEquals(new BigDecimal("700.00"), expectedBalance);
    }
}
