package com.github.pluraliseseverythings.medi.exception;

public class DomainConstraintViolated extends Exception {
    public DomainConstraintViolated(String error) {
        super(error);
    }
}
