package com.springboot.AutomobileInsurance.config;

import com.springboot.AutomobileInsurance.dto.response.ErrorDto;
import com.springboot.AutomobileInsurance.exception.InactiveUserException;
import com.springboot.AutomobileInsurance.exception.InvalidCallException;
import com.springboot.AutomobileInsurance.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler
{

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDto> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ErrorDto error = new ErrorDto(
                ex.getMessage(),
                HttpStatus.NOT_FOUND.toString(),
                Instant.now()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidCallException.class)
    public ResponseEntity<ErrorDto> handleInvalidCallException(InvalidCallException ex) {
        ErrorDto error = new ErrorDto(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.toString(),
                Instant.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorDto error = new ErrorDto(
                errorMessage,
                HttpStatus.BAD_REQUEST.toString(),
                Instant.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleGeneralException(Exception ex) {
        ErrorDto error = new ErrorDto(
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                Instant.now()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InactiveUserException.class)
    public ResponseEntity<ErrorDto> handleInactiveUserException(
            InactiveUserException e
    ){
        //always reply the object not the string

        return ResponseEntity
                .status(HttpStatus.PRECONDITION_FAILED)
                .body(
                        new ErrorDto(e.getMessage(),
                                "Contact ADMIN admin@helpdesk.com",
                                Instant.now())
                );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorDto> handleRuntimeException(RuntimeException ex) {
        ErrorDto error = new ErrorDto(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.toString(),
                Instant.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }







}