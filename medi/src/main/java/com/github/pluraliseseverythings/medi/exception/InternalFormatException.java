package com.github.pluraliseseverythings.medi.exception;

import java.io.IOException;

public class InternalFormatException extends RuntimeException {
    public InternalFormatException(String issue, IOException e) {
        super(issue, e);
    }
}
