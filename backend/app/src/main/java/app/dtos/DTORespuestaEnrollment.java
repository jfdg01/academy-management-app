package app.dtos;

import java.time.LocalDateTime;

/**
 * DTO for student enrollment response in a class
 * Provides detailed information about the operation result
 */
public record DTORespuestaEnrollment(
        boolean success,
        String message,
        Long studentId,
        Long classId,
        String studentName,
        String className,
        LocalDateTime operationDate,
        String operationType // "ENROLLMENT" or "UNENROLLMENT"
) {
    
    /**
     * Constructor for successful operation
     */
    public static DTORespuestaEnrollment success(Long studentId, Long classId, 
                                                String studentName, String className, 
                                                String operationType) {
        return new DTORespuestaEnrollment(
                true,
                "Operation completed successfully",
                studentId,
                classId,
                studentName,
                className,
                LocalDateTime.now(),
                operationType
        );
    }
    
    /**
     * Constructor for failed operation
     */
    public static DTORespuestaEnrollment failure(Long studentId, Long classId, 
                                                String message, String operationType) {
        return new DTORespuestaEnrollment(
                false,
                message,
                studentId,
                classId,
                null,
                null,
                LocalDateTime.now(),
                operationType
        );
    }
}
