package org.library.app.exception;

public class MemberHasBookException extends RuntimeException {

    public MemberHasBookException() {
    }

    public MemberHasBookException(String message) {
        super(message);
    }

    public MemberHasBookException(String message, Throwable cause) {
        super(message, cause);
    }

    public MemberHasBookException(Throwable cause) {
        super(cause);
    }

    public MemberHasBookException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
