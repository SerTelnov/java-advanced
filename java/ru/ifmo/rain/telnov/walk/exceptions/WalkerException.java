package ru.ifmo.rain.telnov.walk.exceptions;

import java.io.IOException;

/**
 * Created by Telnov Sergey on 13.02.2018.
 */
public class WalkerException extends IOException {
    private Exception exception;

    public WalkerException(String message, Exception e) {
        super(message);
        this.exception = e;
    }

    public Exception getException() { return this.exception; }
}
