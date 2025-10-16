package com.example.taskrunner.exception;


import org.springframework.http.*; import org.springframework.web.bind.MethodArgumentNotValidException; import org.springframework.web.bind.annotation.*;


@ControllerAdvice
public class RestExceptionHandler {
@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<ApiError> handleIllegal(IllegalArgumentException ex){ return new ResponseEntity<>(new ApiError(ex.getMessage()), HttpStatus.NOT_FOUND); }
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex){ return new ResponseEntity<>(new ApiError("Validation failed: "+ex.getMessage()), HttpStatus.BAD_REQUEST); }
@ExceptionHandler(RuntimeException.class)
public ResponseEntity<ApiError> handleRuntime(RuntimeException ex){ return new ResponseEntity<>(new ApiError(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR); }
}