package org.library.app.exception;

public class MemberAlreadyExistException extends RuntimeException {
    public MemberAlreadyExistException() {
    }

    public MemberAlreadyExistException(String message) {
        super(message);
    }

    public MemberAlreadyExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public MemberAlreadyExistException(Throwable cause) {
        super(cause);
    }

    public MemberAlreadyExistException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
