package com.springboot.AutomobileInsurance.config;

import com.springboot.AutomobileInsurance.dto.response.ErrorDto;
import com.springboot.AutomobileInsurance.exception.InvalidCallException;
import com.springboot.AutomobileInsurance.exception.OwnershipException;
import com.springboot.AutomobileInsurance.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDto> handleResourceNotFoundException(
            ResourceNotFoundException e
    ) {
        log.warn("Resource lookup failed: {}", e.getMessage());
        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorDto(
                                e.getMessage(),
                                "Id not found in DB",
                                Instant.now()
                        )
                );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>>  handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ) {
        Map<String, String> map = new HashMap<>();

        BindingResult bindingResult = e.getBindingResult();
        List<FieldError> list = bindingResult.getFieldErrors();
        list.forEach(fieldError -> {
            map.put(fieldError.getField(), fieldError.getDefaultMessage());
            log.warn("Field '{}' failed validation: {}", fieldError.getField(), fieldError.getDefaultMessage());
        });

        return ResponseEntity
                .badRequest()
                .body(map);
    }

    @ExceptionHandler(InvalidCallException.class)
    public ResponseEntity<ErrorDto> handleInvalidCallException(
            InvalidCallException e
    ) {
        log.warn("Invalid API invocation: {}", e.getMessage());
        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorDto(
                                e.getMessage(),
                                "Call is Invalid due to malformed data",
                                Instant.now()
                        )
                );
    }

    @ExceptionHandler(OwnershipException.class)
    public ResponseEntity<ErrorDto> handleOwnershipException(
            OwnershipException e
    ) {
        log.warn("Unauthorized access/ownership violation: {}", e.getMessage());
        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorDto(
                                e.getMessage(),
                                "Logged-in User cannot perform this operation",
                                Instant.now()
                        )
                );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorDto> handleRuntimeException(
            RuntimeException e
    ) {
        log.warn("Business runtime rule violated: {}", e.getMessage());
        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorDto(
                                e.getMessage(),
                                "Operation rejected by business rules",
                                Instant.now()
                        )
                );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleException(
            Exception e
    ) {
        log.error("Unhandled internal server exception caught: {}", e.getMessage(), e);
        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorDto(
                                e.getMessage(),
                                "Invalid data passed",
                                Instant.now()
                        )
                );
    }
}