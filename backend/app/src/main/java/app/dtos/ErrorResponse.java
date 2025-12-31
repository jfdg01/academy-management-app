package app.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String errorCode;
    private String path;
    
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> fieldErrors;
    
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> violations;
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String details;
    
    // Additional fields for access denied scenarios
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String requiredRole;
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String currentUserRole;
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String resourceType;
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String resourceId;
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String action;
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String suggestion;
}
