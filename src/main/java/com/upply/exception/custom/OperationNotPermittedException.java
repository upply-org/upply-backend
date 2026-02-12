package com.upply.exception.custom;

public class OperationNotPermittedException extends RuntimeException {
    public OperationNotPermittedException(String message) { super(message); }
}
