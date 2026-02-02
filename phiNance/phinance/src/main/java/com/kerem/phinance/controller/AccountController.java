package com.kerem.phinance.controller;

import com.kerem.phinance.dto.AccountDto;
import com.kerem.phinance.dto.UpdateAccountDto;
import com.kerem.phinance.security.UserPrincipal;
import com.kerem.phinance.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<List<AccountDto>> getAllAccounts(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(accountService.getAllAccounts(userPrincipal.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account by ID")
    public ResponseEntity<AccountDto> getAccountById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id) {
        return ResponseEntity.ok(accountService.getAccountById(userPrincipal.getId(), id));
    }

    @PostMapping
    @Operation(summary = "Create a new account")
    public ResponseEntity<AccountDto> createAccount(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody AccountDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.createAccount(userPrincipal.getId(), dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an account")
    public ResponseEntity<AccountDto> updateAccount(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id,
            @Valid @RequestBody UpdateAccountDto dto) {
        return ResponseEntity.ok(accountService.updateAccount(userPrincipal.getId(), id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Archive an account")
    public ResponseEntity<Map<String, String>> archiveAccount(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id) {
        accountService.archiveAccount(userPrincipal.getId(), id);
        return ResponseEntity.ok(Map.of("message", "Account archived successfully"));
    }
}
