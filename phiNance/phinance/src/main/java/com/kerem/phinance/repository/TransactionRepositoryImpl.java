package com.kerem.phinance.repository;

import com.kerem.phinance.model.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    private List<Criteria> buildCriteria(
            String userId,
            LocalDate startDate,
            LocalDate endDate,
            String accountId,
            String categoryId,
            String type,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String searchQuery
    ) {
        List<Criteria> criteria = new ArrayList<>();

        // Always filter by userId
        criteria.add(Criteria.where("userId").is(userId));

        // Apply date range filter
        if (startDate != null) {
            criteria.add(Criteria.where("date").gte(startDate));
        }
        if (endDate != null) {
            criteria.add(Criteria.where("date").lte(endDate));
        }

        // Apply account filter
        if (accountId != null && !accountId.isEmpty()) {
            criteria.add(Criteria.where("accountId").is(accountId));
        }

        // Apply category filter
        if (categoryId != null && !categoryId.isEmpty()) {
            criteria.add(Criteria.where("categoryId").is(categoryId));
        }

        // Apply type filter
        if (type != null && !type.isEmpty()) {
            try {
                Transaction.TransactionType transactionType = Transaction.TransactionType.valueOf(type);
                criteria.add(Criteria.where("type").is(transactionType));
            } catch (IllegalArgumentException e) {
                // Invalid type, ignore filter
            }
        }

        // Note: Amount filtering is handled in aggregation pipeline for string-to-number conversion

        // Apply search query filter (search in description)
        if (searchQuery != null && !searchQuery.isEmpty()) {
            criteria.add(Criteria.where("description").regex(searchQuery, "i"));
        }

        return criteria;
    }

    @Override
    public Page<Transaction> findByFilters(
            String userId,
            LocalDate startDate,
            LocalDate endDate,
            String accountId,
            String categoryId,
            String type,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String searchQuery,
            Pageable pageable
    ) {
        List<Criteria> criteriaList = buildCriteria(userId, startDate, endDate, accountId, categoryId, 
                type, null, null, searchQuery); // Don't pass amount filters to criteria

        Criteria combinedCriteria = criteriaList.isEmpty() 
                ? new Criteria() 
                : new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));

        // Check if we need to use aggregation (for amount sorting or filtering)
        boolean sortByAmount = pageable.getSort().stream()
                .anyMatch(order -> "amount".equals(order.getProperty()));
        boolean hasAmountFilter = (minAmount != null && minAmount.compareTo(BigDecimal.ZERO) > 0) 
                || (maxAmount != null && maxAmount.compareTo(BigDecimal.ZERO) > 0);

        if (sortByAmount || hasAmountFilter) {
            // Use aggregation for numeric amount operations
            Sort.Order amountOrder = pageable.getSort().stream()
                    .filter(order -> "amount".equals(order.getProperty()))
                    .findFirst()
                    .orElse(null);

            List<org.springframework.data.mongodb.core.aggregation.AggregationOperation> operations = new ArrayList<>();
            
            // Match criteria
            operations.add(match(combinedCriteria));
            
            // Add computed field for numeric amount
            operations.add(project()
                    .andExpression("userId").as("userId")
                    .andExpression("accountId").as("accountId")
                    .andExpression("type").as("type")
                    .andExpression("{$toDouble: '$amount'}").as("amountNumeric")
                    .andExpression("amount").as("amount")
                    .andExpression("categoryId").as("categoryId")
                    .andExpression("description").as("description")
                    .andExpression("date").as("date")
                    .andExpression("recurring").as("recurring")
                    .andExpression("recurrencePattern").as("recurrencePattern")
                    .andExpression("autoGenerated").as("autoGenerated")
                    .andExpression("transferToAccountId").as("transferToAccountId")
                    .andExpression("linkedTransactionId").as("linkedTransactionId")
                    .andExpression("attachmentUrls").as("attachmentUrls")
                    .andExpression("createdAt").as("createdAt")
                    .andExpression("updatedAt").as("updatedAt")
                    .andExpression("_id").as("_id"));
            
            // Apply amount filtering if needed
            if (hasAmountFilter) {
                Criteria amountCriteria = new Criteria();
                if (minAmount != null && minAmount.compareTo(BigDecimal.ZERO) > 0) {
                    amountCriteria = amountCriteria.and("amountNumeric").gte(minAmount.doubleValue());
                }
                if (maxAmount != null && maxAmount.compareTo(BigDecimal.ZERO) > 0) {
                    amountCriteria = amountCriteria.and("amountNumeric").lte(maxAmount.doubleValue());
                }
                operations.add(match(amountCriteria));
            }
            
            // Apply sorting
            if (amountOrder != null) {
                operations.add(sort(amountOrder.getDirection(), "amountNumeric"));
            } else if (pageable.getSort().isSorted()) {
                operations.add(sort(pageable.getSort()));
            }
            
            // Count total before pagination
            Aggregation countAggregation = Aggregation.newAggregation(operations);
            long total = mongoTemplate.aggregate(countAggregation, "transactions", Transaction.class)
                    .getMappedResults().size();
            
            // Add pagination
            operations.add(skip((long) pageable.getPageNumber() * pageable.getPageSize()));
            operations.add(limit(pageable.getPageSize()));

            Aggregation aggregation = Aggregation.newAggregation(operations);
            AggregationResults<Transaction> results = mongoTemplate.aggregate(
                    aggregation, "transactions", Transaction.class);
            List<Transaction> transactions = results.getMappedResults();

            return new PageImpl<>(transactions, pageable, total);
        } else {
            // Use regular query for other cases
            Query query = new Query();
            if (!criteriaList.isEmpty()) {
                query.addCriteria(combinedCriteria);
            }

            long total = mongoTemplate.count(query, Transaction.class);
            query.with(pageable);
            List<Transaction> transactions = mongoTemplate.find(query, Transaction.class);

            return new PageImpl<>(transactions, pageable, total);
        }
    }

    @Override
    public List<Transaction> findAllByFilters(
            String userId,
            LocalDate startDate,
            LocalDate endDate,
            String accountId,
            String categoryId,
            String type,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String searchQuery,
            Sort sort
    ) {
        List<Criteria> criteriaList = buildCriteria(userId, startDate, endDate, accountId, categoryId, 
                type, null, null, searchQuery); // Don't pass amount filters to criteria

        Criteria combinedCriteria = criteriaList.isEmpty() 
                ? new Criteria() 
                : new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));

        // Check if we need to use aggregation (for amount sorting or filtering)
        boolean sortByAmount = sort.stream()
                .anyMatch(order -> "amount".equals(order.getProperty()));
        boolean hasAmountFilter = (minAmount != null && minAmount.compareTo(BigDecimal.ZERO) > 0) 
                || (maxAmount != null && maxAmount.compareTo(BigDecimal.ZERO) > 0);

        if (sortByAmount || hasAmountFilter) {
            // Use aggregation for numeric amount operations
            Sort.Order amountOrder = sort.stream()
                    .filter(order -> "amount".equals(order.getProperty()))
                    .findFirst()
                    .orElse(null);

            List<org.springframework.data.mongodb.core.aggregation.AggregationOperation> operations = new ArrayList<>();
            
            operations.add(match(combinedCriteria));
            operations.add(project()
                    .andExpression("userId").as("userId")
                    .andExpression("accountId").as("accountId")
                    .andExpression("type").as("type")
                    .andExpression("{$toDouble: '$amount'}").as("amountNumeric")
                    .andExpression("amount").as("amount")
                    .andExpression("categoryId").as("categoryId")
                    .andExpression("description").as("description")
                    .andExpression("date").as("date")
                    .andExpression("recurring").as("recurring")
                    .andExpression("recurrencePattern").as("recurrencePattern")
                    .andExpression("autoGenerated").as("autoGenerated")
                    .andExpression("transferToAccountId").as("transferToAccountId")
                    .andExpression("linkedTransactionId").as("linkedTransactionId")
                    .andExpression("attachmentUrls").as("attachmentUrls")
                    .andExpression("createdAt").as("createdAt")
                    .andExpression("updatedAt").as("updatedAt")
                    .andExpression("_id").as("_id"));
            
            // Apply amount filtering if needed
            if (hasAmountFilter) {
                Criteria amountCriteria = new Criteria();
                if (minAmount != null && minAmount.compareTo(BigDecimal.ZERO) > 0) {
                    amountCriteria = amountCriteria.and("amountNumeric").gte(minAmount.doubleValue());
                }
                if (maxAmount != null && maxAmount.compareTo(BigDecimal.ZERO) > 0) {
                    amountCriteria = amountCriteria.and("amountNumeric").lte(maxAmount.doubleValue());
                }
                operations.add(match(amountCriteria));
            }
            
            // Apply sorting
            if (amountOrder != null) {
                operations.add(sort(amountOrder.getDirection(), "amountNumeric"));
            } else if (sort.isSorted()) {
                operations.add(sort(sort));
            }

            Aggregation aggregation = Aggregation.newAggregation(operations);
            AggregationResults<Transaction> results = mongoTemplate.aggregate(
                    aggregation, "transactions", Transaction.class);
            return results.getMappedResults();
        } else {
            // Use regular query for other cases
            Query query = new Query();
            if (!criteriaList.isEmpty()) {
                query.addCriteria(combinedCriteria);
            }
            query.with(sort);
            return mongoTemplate.find(query, Transaction.class);
        }
    }
}
