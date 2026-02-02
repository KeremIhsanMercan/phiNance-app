package com.kerem.phinance.service;

import com.kerem.phinance.dto.TransactionDto;
import com.kerem.phinance.model.Transaction;
import com.kerem.phinance.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecurringTransactionScheduler {

    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;

    // Run every two weeks (1,209,600,000 milliseconds = 14 days)
    @Scheduled(fixedRate = 1_209_600_000)
    public void processRecurringTransactions() {
        log.info("Starting recurring transactions processing...");

        List<Transaction> recurringTransactions = transactionRepository.findByRecurringTrue();
        YearMonth currentMonth = YearMonth.now();
        LocalDate today = LocalDate.now();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");

        int processed = 0;
        int created = 0;

        for (Transaction transaction : recurringTransactions) {
            try {
                YearMonth transactionMonth = YearMonth.from(transaction.getDate());

                // Skip if transaction is in current or future month
                if (!transactionMonth.isBefore(currentMonth)) {
                    continue;
                }

                processed++;

                // Generate the new transaction name with current month
                String currentMonthName = currentMonth.format(monthFormatter);
                String newTransactionName = transaction.getDescription() + " " + currentMonthName;

                // Check if a transaction with this name already exists for this user
                boolean alreadyExists = transactionRepository.findByUserId(transaction.getUserId()).stream()
                        .anyMatch(t -> newTransactionName.equals(t.getDescription()));

                if (alreadyExists) {
                    log.debug("Transaction '{}' already exists for user {}", newTransactionName, transaction.getUserId());
                    continue;
                }

                // Create a new transaction for the current month
                TransactionDto newTransactionDto = new TransactionDto();
                newTransactionDto.setAccountId(transaction.getAccountId());
                newTransactionDto.setType(transaction.getType());
                newTransactionDto.setAmount(transaction.getAmount());
                newTransactionDto.setCategoryId(transaction.getCategoryId());
                newTransactionDto.setDescription(newTransactionName);
                newTransactionDto.setDate(today); // Use current date
                newTransactionDto.setRecurring(false); // Not a recurring transaction
                newTransactionDto.setRecurrencePattern(null);
                newTransactionDto.setTransferToAccountId(transaction.getTransferToAccountId());

                transactionService.createTransaction(transaction.getUserId(), newTransactionDto);
                created++;
                log.info("Created recurring transaction '{}' for user {}", newTransactionName, transaction.getUserId());

            } catch (Exception e) {
                log.error("Error processing recurring transaction {}: {}", transaction.getId(), e.getMessage());
            }
        }

        log.info("Recurring transactions processing completed. Processed: {}, Created: {}", processed, created);
    }
}
