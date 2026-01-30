package com.kerem.phinance.service;

import com.kerem.phinance.dto.AccountDto;
import com.kerem.phinance.exception.ResourceNotFoundException;
import com.kerem.phinance.model.Account;
import com.kerem.phinance.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

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
        Account account = new Account();
        account.setUserId(userId);
        account.setName(dto.getName());
        account.setType(dto.getType());
        account.setInitialBalance(dto.getInitialBalance() != null ? dto.getInitialBalance() : BigDecimal.ZERO);
        account.setCurrentBalance(dto.getInitialBalance() != null ? dto.getInitialBalance() : BigDecimal.ZERO);
        account.setCurrency(dto.getCurrency());
        account.setDescription(dto.getDescription());
        account.setColor(dto.getColor());
        account.setIcon(dto.getIcon());

        Account saved = accountRepository.save(account);
        return mapToDto(saved);
    }

    public AccountDto updateAccount(String userId, String accountId, AccountDto dto) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        account.setName(dto.getName());
        account.setType(dto.getType());
        account.setCurrency(dto.getCurrency());
        account.setDescription(dto.getDescription());
        account.setColor(dto.getColor());
        account.setIcon(dto.getIcon());

        Account saved = accountRepository.save(account);
        return mapToDto(saved);
    }

    public void archiveAccount(String userId, String accountId) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        account.setArchived(true);
        accountRepository.save(account);
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
