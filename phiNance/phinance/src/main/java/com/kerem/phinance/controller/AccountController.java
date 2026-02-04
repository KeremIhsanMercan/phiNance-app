package com.kerem.phinance.controller;

import com.kerem.phinance.dto.AccountDto;
import com.kerem.phinance.dto.UpdateAccountDto;
import com.kerem.phinance.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Account management APIs")
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    @Operation(summary = "Get all accounts")
    public ResponseEntity<Page<AccountDto>> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(accountService.getAccountsPaginated(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account by ID")
    public ResponseEntity<AccountDto> getAccountById(
            @PathVariable String id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new account")
    public ResponseEntity<AccountDto> createAccount(
            @Valid @RequestBody AccountDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.createAccount(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an account")
    public ResponseEntity<AccountDto> updateAccount(
            @PathVariable String id,
            @Valid @RequestBody UpdateAccountDto dto) {
        return ResponseEntity.ok(accountService.updateAccount(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Archive an account")
    public ResponseEntity<Map<String, String>> archiveAccount(
            @PathVariable String id) {
        accountService.archiveAccount(id);
        return ResponseEntity.ok(Map.of("message", "Account archived successfully"));
    }
}
