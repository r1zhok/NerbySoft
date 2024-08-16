package org.library.app.exception;

public class MemberAlreadyHaveThisBookException extends RuntimeException {
    public MemberAlreadyHaveThisBookException() {
    }

    public MemberAlreadyHaveThisBookException(String message) {
        super(message);
    }

    public MemberAlreadyHaveThisBookException(String message, Throwable cause) {
        super(message, cause);
    }

    public MemberAlreadyHaveThisBookException(Throwable cause) {
        super(cause);
    }

    public MemberAlreadyHaveThisBookException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
