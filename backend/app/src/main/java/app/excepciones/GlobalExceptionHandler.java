package app.excepciones;

import app.dtos.ErrorResponse;
import app.util.AccessDeniedUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // Custom API Exceptions
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, WebRequest request) {
        log.warn("[ERROR] API Exception: {} - {}", ex.getErrorCode(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(ex.getStatus().value())
                .error(ex.getStatus().getReasonPhrase())
                .message(ex.getUserMessage())
                .errorCode(ex.getErrorCode())
                .path(getPath(request))
                .build();
        
        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

    // Validation Exceptions
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex, WebRequest request) {
        log.warn("[ERROR] Validation Exception: {}", ex.getMessage());
        
        ErrorResponse.ErrorResponseBuilder builder = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(ex.getStatus().value())
                .error(ex.getStatus().getReasonPhrase())
                .message(ex.getUserMessage())
                .errorCode(ex.getErrorCode())
                .path(getPath(request));
        
        if (ex.getFieldErrors() != null) {
            builder.fieldErrors(ex.getFieldErrors());
        }
        
        return new ResponseEntity<>(builder.build(), ex.getStatus());
    }

    // MethodArgumentNotValidException - @Valid validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        log.warn("[ERROR] Validation error in request body: {}", ex.getMessage());
        
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                    error -> error.getField(),
                    error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Valor inválido",
                    (existing, replacement) -> existing
                ));
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Los datos enviados contienen errores")
                .errorCode("VALIDATION_ERROR")
                .fieldErrors(fieldErrors)
                .path(getPath(request))
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // ConstraintViolationException - @PathVariable and @RequestParam validation
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        
        log.warn("[ERROR] Parameter validation error: {}", ex.getMessage());
        
        Map<String, String> violations = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                    violation -> getPropertyName(violation),
                    ConstraintViolation::getMessage,
                    (existing, replacement) -> existing
                ));
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Los parámetros enviados son inválidos")
                .errorCode("PARAMETER_VALIDATION_ERROR")
                .violations(violations)
                .path(getPath(request))
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // MethodArgumentTypeMismatchException - Type conversion errors (like "NaN" to Long)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        
        log.warn("[ERROR] Type mismatch error: {} - {}", ex.getName(), ex.getValue());
        
        String message = String.format("El parámetro '%s' con valor '%s' no es válido", 
                ex.getName(), ex.getValue());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(message)
                .errorCode("TYPE_MISMATCH_ERROR")
                .path(getPath(request))
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // HttpMessageNotReadableException - JSON parsing errors
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {
        
        log.warn("[ERROR] JSON parse error: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("El JSON enviado no es válido")
                .errorCode("JSON_PARSE_ERROR")
                .path(getPath(request))
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // HttpMediaTypeNotSupportedException - Content-Type not supported
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, WebRequest request) {
        
        log.warn("[ERROR] Media type not supported: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
                .error(HttpStatus.UNSUPPORTED_MEDIA_TYPE.getReasonPhrase())
                .message("El tipo de contenido no es soportado")
                .errorCode("UNSUPPORTED_MEDIA_TYPE")
                .path(getPath(request))
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    // Resource Not Found Exceptions
    @ExceptionHandler({EntidadNoEncontradaException.class, AlumnoNoEncontradoException.class})
    public ResponseEntity<ErrorResponse> handleResourceNotFound(Exception ex, WebRequest request) {
        log.warn("[ERROR] Resource not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .errorCode("RESOURCE_NOT_FOUND")
                .path(getPath(request))
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // NoHandlerFoundException and NoResourceFoundException - 404 errors
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(Exception ex, WebRequest request) {
        log.warn("[ERROR] No handler found for request: {}", getPath(request));
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message("El endpoint solicitado no existe")
                .errorCode("ENDPOINT_NOT_FOUND")
                .path(getPath(request))
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // Security/Authentication Exceptions
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        log.warn("[ERROR] Authentication failed: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message("Usuario o contraseña incorrectos")
                .errorCode("AUTHENTICATION_FAILED")
                .path(getPath(request))
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({DisabledException.class, LockedException.class, AccountExpiredException.class, CredentialsExpiredException.class})
    public ResponseEntity<ErrorResponse> handleAccountIssues(Exception ex, WebRequest request) {
        log.warn("[ERROR] Account issue: {}", ex.getMessage());
        
        String message;
        String errorCode;
        
        if (ex instanceof DisabledException) {
            message = "Tu cuenta está deshabilitada. Contacta con el administrador.";
            errorCode = "ACCOUNT_DISABLED";
        } else if (ex instanceof LockedException) {
            message = "Tu cuenta está bloqueada. Contacta con el administrador.";
            errorCode = "ACCOUNT_LOCKED";
        } else if (ex instanceof AccountExpiredException) {
            message = "Tu cuenta ha expirado. Contacta con el administrador.";
            errorCode = "ACCOUNT_EXPIRED";
        } else {
            message = "Tu contraseña ha expirado. Debes cambiarla.";
            errorCode = "CREDENTIALS_EXPIRED";
        }
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message(message)
                .errorCode(errorCode)
                .path(getPath(request))
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    // Authorization Exceptions
    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<ErrorResponse> handleAccessDenied(Exception ex, WebRequest request) {
        log.warn("[ERROR] Access denied: {}", ex.getMessage());
        
        // Extract relevant information for better error reporting
        String path = getPath(request);
        String resourceType = AccessDeniedUtils.extractResourceType(path);
        String resourceId = AccessDeniedUtils.extractResourceId(path);
        String action = AccessDeniedUtils.extractAction(request);
        String requiredRole = AccessDeniedUtils.extractRequiredRole(ex.getMessage());
        
        // Get current authentication context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserRole = AccessDeniedUtils.extractCurrentUserRole(authentication);
        
        // Determine the type of access denied
        boolean isAuthRequired = AccessDeniedUtils.isAuthenticationRequired(authentication);
        boolean isInsufficientPrivileges = AccessDeniedUtils.isInsufficientPrivileges(authentication, requiredRole);
        
        // Generate appropriate message and suggestion
        String message;
        String suggestion;
        
        if (isAuthRequired) {
            message = "Autenticación requerida para acceder a este recurso";
            suggestion = "Inicia sesión para acceder a esta funcionalidad";
        } else if (isInsufficientPrivileges) {
            message = "No tienes permisos suficientes para realizar esta acción";
            suggestion = AccessDeniedUtils.generateSuggestion(requiredRole, currentUserRole, resourceType, action);
        } else {
            message = "Acceso denegado a este recurso";
            suggestion = "Verifica que tienes los permisos necesarios para realizar esta acción";
        }
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message(message)
                .errorCode("ACCESS_DENIED")
                .path(path)
                .requiredRole(requiredRole)
                .currentUserRole(currentUserRole)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .action(action)
                .suggestion(suggestion)
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    // Data Integrity Violation
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, WebRequest request) {
        
        log.warn("[ERROR] Data integrity violation: {}", ex.getMessage());
        
        String userMessage = "Ya existe un registro con los datos proporcionados";
        String exceptionMessage = ex.getMessage();
        
        if (exceptionMessage != null) {
            if (exceptionMessage.contains("usuarios_dni_key")) {
                userMessage = "Ya existe un usuario con este DNI";
            } else if (exceptionMessage.contains("usuarios_usuario_key")) {
                userMessage = "Ya existe un usuario con este nombre de usuario";
            } else if (exceptionMessage.contains("usuarios_email_key")) {
                userMessage = "Ya existe un usuario con este email";
            } else if (exceptionMessage.contains("duplicate key")) {
                userMessage = "Ya existe un registro con estos datos únicos";
            }
        }
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(userMessage)
                .errorCode("DUPLICATE_DATA")
                .path(getPath(request))
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    // IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        log.warn("[ERROR] Illegal argument: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .errorCode("INVALID_ARGUMENT")
                .path(getPath(request))
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Generic Exception Handler - Last resort
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        // Only log the error message, not the full stack trace for cleaner logs
        log.error("[ERROR] Unexpected error: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("Ha ocurrido un error inesperado. Por favor, inténtalo de nuevo más tarde.")
                .errorCode("INTERNAL_SERVER_ERROR")
                .path(getPath(request))
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Helper methods
    private String getPropertyName(ConstraintViolation<?> violation) {
        String propertyPath = violation.getPropertyPath().toString();
        return propertyPath.contains(".") ? 
               propertyPath.substring(propertyPath.lastIndexOf('.') + 1) : 
               propertyPath;
    }

    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
} 