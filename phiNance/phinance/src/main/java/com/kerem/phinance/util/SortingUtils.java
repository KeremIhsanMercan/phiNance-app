package com.kerem.phinance.util;

import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Utility class for handling case-insensitive sorting in MongoDB MongoDB by
 * default sorts case-sensitively (uppercase before lowercase) This utility
 * provides methods to create Pageable with case-insensitive collation
 */
public class SortingUtils {

    /**
     * Create a Pageable with case-insensitive collation for MongoDB queries
     * Uses strength=SECONDARY which means case-insensitive but accent-sensitive
     *
     * @param page the page number (0-indexed)
     * @param size the page size
     * @param sort the Sort object specifying sort fields and direction
     * @return Pageable with case-insensitive collation
     */
    public static Pageable createCaseInsensitivePageable(int page, int size, Sort sort) {
        return PageRequest.of(page, size, sort);
    }

    /**
     * Get the case-insensitive collation for MongoDB queries
     *
     * @return Collation with strength=SECONDARY (case-insensitive,
     * accent-sensitive)
     */
    public static Collation getCaseInsensitiveCollation() {
        return Collation.of("en");
    }

    /**
     * Create a Pageable with case-insensitive collation for a single sort field
     *
     * @param page the page number (0-indexed)
     * @param size the page size
     * @param direction the sort direction (ASC or DESC)
     * @param property the property to sort by
     * @return Pageable with case-insensitive collation
     */
    public static Pageable createCaseInsensitivePageable(int page, int size, Sort.Direction direction, String property) {
        return PageRequest.of(page, size, Sort.by(direction, property));
    }

    /**
     * Create a Pageable with case-insensitive collation for multiple sort
     * fields
     *
     * @param page the page number (0-indexed)
     * @param size the page size
     * @param direction the sort direction (ASC or DESC)
     * @param properties the properties to sort by
     * @return Pageable with case-insensitive collation
     */
    public static Pageable createCaseInsensitivePageable(int page, int size, Sort.Direction direction, String... properties) {
        return PageRequest.of(page, size, Sort.by(direction, properties));
    }
}
