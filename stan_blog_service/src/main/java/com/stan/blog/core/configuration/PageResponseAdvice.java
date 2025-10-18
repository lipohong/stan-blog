package com.stan.blog.core.configuration;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.data.domain.Page;

import com.stan.blog.core.dto.PageResponse;

/**
 * Wrap all Page<?> responses to PageResponse with fields
 * records/current/pages/total/size to match frontend expectations.
 */
@RestControllerAdvice
public class PageResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // Apply advice broadly; actual wrapping is guarded in beforeBodyWrite
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {
        if (body instanceof Page<?> page) {
            return PageResponse.from(page);
        }
        return body;
    }
}