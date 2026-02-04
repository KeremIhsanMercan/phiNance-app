package com.kerem.phinance.service;

import com.kerem.phinance.dto.DashboardDto;
import com.kerem.phinance.dto.TransactionDto;
import com.kerem.phinance.model.Account;
import com.kerem.phinance.model.Category;
import com.kerem.phinance.model.Transaction;
import com.kerem.phinance.repository.AccountRepository;
import com.kerem.phinance.repository.CategoryRepository;
import com.kerem.phinance.repository.TransactionRepository;
import com.kerem.phinance.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    public DashboardDto getDashboard() {
        String userId = SecurityUtils.getCurrentUserId();
        // Get all active accounts
        List<Account> accounts = accountRepository.findByUserIdAndArchivedFalse(userId);

        // Calculate total net worth
        BigDecimal totalNetWorth = accounts.stream()
                .map(Account::getCurrentBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get current month transactions
        YearMonth currentMonth = YearMonth.now();
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();

        List<Transaction> currentMonthTransactions = transactionRepository
                .findByUserIdAndDateBetween(userId, startOfMonth, endOfMonth);

        // Calculate income and expenses
        BigDecimal totalIncome = currentMonthTransactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = currentMonthTransactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get account summaries
        List<DashboardDto.AccountSummary> accountSummaries = accounts.stream()
                .map(a -> DashboardDto.AccountSummary.builder()
                .id(a.getId())
                .name(a.getName())
                .type(a.getType().name())
                .balance(a.getCurrentBalance())
                .currency(a.getCurrency())
                .color(a.getColor())
                .build())
                .collect(Collectors.toList());

        // Get category expenses
        List<DashboardDto.CategoryExpense> categoryExpenses = calculateCategoryExpenses(
                userId, currentMonthTransactions, totalExpenses);

        // Get monthly data for the last 6 months
        List<DashboardDto.MonthlyData> monthlyData = calculateMonthlyData(userId);

        // Get recent transactions
        List<TransactionDto> recentTransactions = transactionRepository
                .findByUserId(userId, PageRequest.of(0, 5, Sort.by("date").descending()))
                .stream()
                .map(this::mapToTransactionDto)
                .collect(Collectors.toList());

        return DashboardDto.builder()
                .totalNetWorth(totalNetWorth)
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .accountSummaries(accountSummaries)
                .categoryExpenses(categoryExpenses)
                .monthlyData(monthlyData)
                .recentTransactions(recentTransactions)
                .build();
    }

    private List<DashboardDto.CategoryExpense> calculateCategoryExpenses(
            String userId, List<Transaction> transactions, BigDecimal totalExpenses) {

        // Group expenses by category (including null for uncategorized)
        Map<String, BigDecimal> expensesByCategory = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        t -> t.getCategoryId() != null ? t.getCategoryId() : "uncategorized",
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)));

        List<Category> categories = categoryRepository.findByUserIdOrIsDefaultTrue(userId);
        Map<String, Category> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        return expensesByCategory.entrySet().stream()
                .map(entry -> {
                    String categoryId = entry.getKey();
                    Category category = categoryMap.get(categoryId);
                    double percentage = totalExpenses.compareTo(BigDecimal.ZERO) == 0 ? 0
                            : entry.getValue().divide(totalExpenses, 4, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100)).doubleValue();

                    // Handle uncategorized expenses
                    if ("uncategorized".equals(categoryId)) {
                        return DashboardDto.CategoryExpense.builder()
                                .categoryId(null)
                                .categoryName("Uncategorized")
                                .color("#9CA3AF") // gray-400
                                .amount(entry.getValue())
                                .percentage(percentage)
                                .build();
                    }

                    return DashboardDto.CategoryExpense.builder()
                            .categoryId(entry.getKey())
                            .categoryName(category != null ? category.getName() : "Category Deleted")
                            .color(category != null ? category.getColor() : "#808080")
                            .amount(entry.getValue())
                            .percentage(percentage)
                            .build();
                })
                .sorted((a, b) -> b.getAmount().compareTo(a.getAmount()))
                .collect(Collectors.toList());
    }

    private List<DashboardDto.MonthlyData> calculateMonthlyData(String userId) {
        List<DashboardDto.MonthlyData> monthlyData = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();

        for (int i = 5; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            LocalDate start = month.atDay(1);
            LocalDate end = month.atEndOfMonth();

            List<Transaction> transactions = transactionRepository
                    .findByUserIdAndDateBetween(userId, start, end);

            BigDecimal income = transactions.stream()
                    .filter(t -> t.getType() == Transaction.TransactionType.INCOME)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal expenses = transactions.stream()
                    .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            monthlyData.add(DashboardDto.MonthlyData.builder()
                    .month(month.toString())
                    .income(income)
                    .expenses(expenses)
                    .build());
        }

        return monthlyData;
    }

    private TransactionDto mapToTransactionDto(Transaction transaction) {
        TransactionDto dto = new TransactionDto();
        dto.setId(transaction.getId());
        dto.setAccountId(transaction.getAccountId());
        dto.setType(transaction.getType());
        dto.setAmount(transaction.getAmount());
        dto.setCategoryId(transaction.getCategoryId());
        dto.setDescription(transaction.getDescription());
        dto.setDate(transaction.getDate());
        return dto;
    }
}
