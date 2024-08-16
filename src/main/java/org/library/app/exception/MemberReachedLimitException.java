package org.library.app.exception;

public class MemberReachedLimitException extends RuntimeException {
    public MemberReachedLimitException() {
    }

    public MemberReachedLimitException(String message) {
        super(message);
    }

    public MemberReachedLimitException(String message, Throwable cause) {
        super(message, cause);
    }

    public MemberReachedLimitException(Throwable cause) {
        super(cause);
    }

    public MemberReachedLimitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
