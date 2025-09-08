package com.stan.blog.core.exception;

public class StanBlogRuntimeException extends RuntimeException {

    public StanBlogRuntimeException(String message) {
        super(message);
    }

    public StanBlogRuntimeException() {
        super();
    }

    public StanBlogRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public StanBlogRuntimeException(Throwable cause) {
        super(cause);
    }

}
