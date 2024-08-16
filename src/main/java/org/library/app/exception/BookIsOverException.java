package org.library.app.exception;

public class BookIsOverException extends RuntimeException {
    public BookIsOverException() {
    }

    public BookIsOverException(String message) {
        super(message);
    }

    public BookIsOverException(String message, Throwable cause) {
        super(message, cause);
    }

    public BookIsOverException(Throwable cause) {
        super(cause);
    }

    public BookIsOverException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
