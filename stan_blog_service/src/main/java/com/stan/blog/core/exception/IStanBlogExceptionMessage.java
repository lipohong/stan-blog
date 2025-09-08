package com.stan.blog.core.exception;

public interface IStanBlogExceptionMessage {

    String getMessage();

    int getCode();

    void setMessage(String message);

    void setCode(int code);

    default String print() {
        return String.format("{'message': '%s', 'code': '%d'}", this.getMessage(), this.getCode());
    }
}
