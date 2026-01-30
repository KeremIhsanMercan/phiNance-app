package com.kerem.phinance.dto;

import com.kerem.phinance.model.Account;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {

    private String id;

    @NotBlank(message = "Account name is required")
    private String name;

    @NotNull(message = "Account type is required")
    private Account.AccountType type;

    @NotNull(message = "Initial balance is required")
    private BigDecimal initialBalance;

    private BigDecimal currentBalance;

    @NotBlank(message = "Currency is required")
    private String currency;

    private String description;

    private String color;

    private String icon;
}
