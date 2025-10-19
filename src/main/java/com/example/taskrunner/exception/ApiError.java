package com.example.taskrunner.exception;
public class ApiError { private final String message; public ApiError(String m){this.message=m;} public String getMessage(){return message;} }