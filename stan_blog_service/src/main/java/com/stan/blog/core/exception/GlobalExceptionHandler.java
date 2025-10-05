package com.stan.blog.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleEmailNotVerifiedException(EmailNotVerifiedException ex) {
        log.warn("Email verification required: {}", ex.getMessage());
        return ErrorResponse.unprocessableEntity(ex.getMessage());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        log.warn("User not found: {}", ex.getMessage());
        return ErrorResponse.unauthorized("Invalid username or password");
    }

    @ExceptionHandler(StanBlogRuntimeException.class)
    public ResponseEntity<ErrorResponse> handleStanBlogRuntimeException(StanBlogRuntimeException ex) {
        log.error("StanBlogRuntimeException: {}", ex.getMessage());
        
        // Check if the exception is related to email already registered
        if (ex.getMessage().contains("email has been registered")) {
            return ErrorResponse.conflict(ex.getMessage());
        }
        
        // Default to BadRequest for other StanBlogRuntimeExceptions
        return ErrorResponse.badRequest(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unhandled exception: ", ex);
        return ErrorResponse.internalServerError("An unexpected error occurred");
    }

    /**
     * Handle bean validation errors on @RequestBody DTOs
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        final String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> toFieldMessage(fe))
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
        log.warn("Validation error: {}", message);
        return ErrorResponse.badRequest(message);
    }

    /**
     * Handle validation errors on @RequestParam/@PathVariable etc.
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex) {
        final String message = ex.getFieldErrors().stream()
                .map(fe -> toFieldMessage(fe))
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
        log.warn("Binding error: {}", message);
        return ErrorResponse.badRequest(message);
    }

    private String toFieldMessage(FieldError fe) {
        String field = fe.getField();
        String defaultMessage = fe.getDefaultMessage();
        return (field == null || field.isBlank()) ? defaultMessage : field + ": " + defaultMessage;
    }

    @ExceptionHandler({MissingServletRequestPartException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> handleMissingMultipart(Exception ex) {
        log.warn("Bad upload request: {}", ex.getMessage());
        return ErrorResponse.badRequest("Missing required file part 'file'");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUpload(MaxUploadSizeExceededException ex) {
        log.warn("Upload too large: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ErrorResponse(413, "Uploaded file is too large"));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorResponse> handleMultipart(MultipartException ex) {
        log.warn("Multipart error: {}", ex.getMessage());
        return ErrorResponse.badRequest("Invalid multipart request");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMedia(HttpMediaTypeNotSupportedException ex) {
        log.warn("Unsupported media type: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(new ErrorResponse(415, "Unsupported media type. Use multipart/form-data for file uploads."));
    }

    @Data
    @AllArgsConstructor
    public static class ErrorResponse {
        private int code;
        private String message;
        
        public static ResponseEntity<ErrorResponse> unauthorized(String message) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(401, message));
        }
        
        public static ResponseEntity<ErrorResponse> unprocessableEntity(String message) {
            return ResponseEntity
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(new ErrorResponse(422, message));
        }
        
        public static ResponseEntity<ErrorResponse> conflict(String message) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(409, message));
        }
        
        public static ResponseEntity<ErrorResponse> badRequest(String message) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(400, message));
        }
        
        public static ResponseEntity<ErrorResponse> internalServerError(String message) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(500, message));
        }
    }
} 
