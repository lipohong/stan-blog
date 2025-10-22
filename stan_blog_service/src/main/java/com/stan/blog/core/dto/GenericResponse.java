package com.stan.blog.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenericResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> GenericResponse<T> ok(T data) {
        return GenericResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> GenericResponse<T> fail(String message) {
        return GenericResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}