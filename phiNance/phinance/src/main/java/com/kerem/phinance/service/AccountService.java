package com.kerem.phinance.service;

import com.kerem.phinance.dto.AccountDto;
import com.kerem.phinance.dto.UpdateAccountDto;
import com.kerem.phinance.exception.ResourceNotFoundException;
import com.kerem.phinance.model.Account;
import com.kerem.phinance.model.Transaction;
import com.kerem.phinance.model.User;
import com.kerem.phinance.repository.AccountRepository;
import com.kerem.phinance.repository.TransactionRepository;
import com.kerem.phinance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;

    public AccountService(AccountRepository accountRepository,
            UserRepository userRepository,
            TransactionRepository transactionRepository,
            @Lazy TransactionService transactionService) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.transactionService = transactionService;
    }

    public List<AccountDto> getAllAccounts(String userId) {
        return accountRepository.findByUserIdAndArchivedFalse(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public AccountDto getAccountById(String userId, String accountId) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));
        return mapToDto(account);
    }

    public AccountDto createAccount(String userId, AccountDto dto) {
        // Get user's preferred currency
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Account account = new Account();
        account.setUserId(userId);
        account.setName(dto.getName());
        account.setType(dto.getType());
        account.setInitialBalance(dto.getInitialBalance() != null ? dto.getInitialBalance() : BigDecimal.ZERO);
        account.setCurrentBalance(dto.getInitialBalance() != null ? dto.getInitialBalance() : BigDecimal.ZERO);
        account.setCurrency(user.getPreferredCurrency());
        account.setDescription(dto.getDescription());
        account.setColor(dto.getColor());
        account.setIcon(dto.getIcon());

        Account saved = accountRepository.save(account);
        return mapToDto(saved);
    }

    public AccountDto updateAccount(String userId, String accountId, UpdateAccountDto dto) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        account.setName(dto.getName());
        account.setType(dto.getType());
        account.setDescription(dto.getDescription());
        account.setColor(dto.getColor());
        account.setIcon(dto.getIcon());

        Account saved = accountRepository.save(account);
        return mapToDto(saved);
    }

    public void archiveAccount(String userId, String accountId) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        // Get all transactions related to this account
        List<Transaction> relatedTransactions = transactionRepository
                .findByUserIdAndAccountIdOrTransferToAccountId(userId, accountId, accountId);

        // Revert and delete each transaction (this handles balance reversal, budget updates, and goal contributions)
        for (Transaction transaction : relatedTransactions) {
            transactionService.deleteTransaction(userId, transaction.getId());
        }

        account.setArchived(true);
        accountRepository.save(account);
    }

    public void deleteAccount(String userId, String accountId) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        // Get all transactions related to this account
        List<Transaction> relatedTransactions = transactionRepository
                .findByUserIdAndAccountIdOrTransferToAccountId(userId, accountId, accountId);

        // Revert and delete each transaction (this handles balance reversal, budget updates, and goal contributions)
        for (Transaction transaction : relatedTransactions) {
            transactionService.deleteTransaction(userId, transaction.getId());
        }

        // Delete the account
        accountRepository.delete(account);
    }

    public void updateBalance(String accountId, BigDecimal amount, boolean isAddition) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        BigDecimal newBalance = isAddition
                ? account.getCurrentBalance().add(amount)
                : account.getCurrentBalance().subtract(amount);

        account.setCurrentBalance(newBalance);
        accountRepository.save(account);
    }

    public boolean accountBelongsToUser(String accountId, String userId) {
        return accountRepository.existsByIdAndUserId(accountId, userId);
    }

    private AccountDto mapToDto(Account account) {
        AccountDto dto = new AccountDto();
        dto.setId(account.getId());
        dto.setName(account.getName());
        dto.setType(account.getType());
        dto.setInitialBalance(account.getInitialBalance());
        dto.setCurrentBalance(account.getCurrentBalance());
        dto.setCurrency(account.getCurrency());
        dto.setDescription(account.getDescription());
        dto.setColor(account.getColor());
        dto.setIcon(account.getIcon());
        return dto;
    }
}
