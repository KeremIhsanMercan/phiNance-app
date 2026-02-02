package com.kerem.phinance.dto;

import com.kerem.phinance.model.Account;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountDto {

    @NotBlank(message = "Account name is required")
    private String name;

    @NotNull(message = "Account type is required")
    private Account.AccountType type;

    private String description;

    private String color;

    private String icon;
}
