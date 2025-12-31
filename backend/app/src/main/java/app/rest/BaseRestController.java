package app.rest;

import app.dtos.DTORespuestaPaginada;
import app.excepciones.ValidationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Base controller that provides common functionality for all REST controllers
 * Implements the standard pagination and validation pattern
 */
@Validated
public abstract class BaseRestController {

    /**
     * Validates and creates standard pagination parameters
     * 
     * @param page Page number (0-indexed)
     * @param size Page size
     * @param sortBy Field to sort by
     * @param sortDirection Sort direction (ASC/DESC)
     * @return Configured Pageable
     */
    protected Pageable createPageable(
            @Min(0) int page,
            @Min(1) @Max(100) int size,
            @Size(max = 50) String sortBy,
            @Pattern(regexp = "(?i)^(ASC|DESC)$") String sortDirection) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        return PageRequest.of(page, size, sort);
    }

    /**
     * Creates a standard paginated response from a Spring Data Page
     * 
     * @param page Spring Data Page
     * @param sortBy Field by which it is sorted
     * @param sortDirection Sort direction
     * @return Standard DTORespuestaPaginada
     */
    protected <T> DTORespuestaPaginada<T> createPaginatedResponse(
            Page<T> page, 
            String sortBy, 
            String sortDirection) {
        
        return DTORespuestaPaginada.fromPage(page, sortBy, sortDirection);
    }

    /**
     * Validates that the page size is within allowed limits
     * 
     * @param size Requested page size
     * @return Validated page size
     * @throws ValidationException if size is invalid
     */
    protected int validatePageSize(int size) {
        if (size < 1) {
            throw new ValidationException("Page size must be greater than 0");
        }
        if (size > 100) {
            throw new ValidationException("Page size cannot exceed 100");
        }
        return size;
    }

    /**
     * Validates that the page number is valid
     * 
     * @param page Requested page number
     * @return Validated page number
     * @throws ValidationException if page is invalid
     */
    protected int validatePageNumber(int page) {
        if (page < 0) {
            throw new ValidationException("Page number cannot be negative");
        }
        return page;
    }

    /**
     * Validates that the sort field is valid
     * 
     * @param sortBy Requested sort field
     * @param allowedFields Allowed fields for sorting
     * @return Validated sort field
     * @throws ValidationException if sortBy is not allowed
     */
    protected String validateSortBy(String sortBy, String... allowedFields) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "id"; // Default field
        }
        
        // If allowed fields are specified, validate against them
        if (allowedFields.length > 0) {
            for (String allowedField : allowedFields) {
                if (allowedField.equalsIgnoreCase(sortBy)) {
                    return allowedField;
                }
            }
            throw new ValidationException("Invalid sort field: " + sortBy);
        }
        
        return sortBy;
    }

    /**
     * Validates that the sort direction is valid
     * 
     * @param sortDirection Requested sort direction
     * @return Validated sort direction
     * @throws ValidationException if sortDirection is invalid
     */
    protected String validateSortDirection(String sortDirection) {
        if (sortDirection == null || sortDirection.trim().isEmpty()) {
            return "ASC";
        }
        
        String upperDirection = sortDirection.toUpperCase();
        if ("ASC".equals(upperDirection) || "DESC".equals(upperDirection)) {
            return upperDirection;
        }
        
        throw new ValidationException("Invalid sort direction: " + sortDirection);
    }
} 