package com.stan.blog.core.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private long current;
    private long size;
    private long total;
    private long pages;
    private List<T> records;

    public static <T> PageResponse<T> from(Page<T> page) {
        PageResponse<T> response = new PageResponse<>();
        response.setCurrent(page.getNumber() + 1L);
        response.setSize(page.getSize());
        response.setTotal(page.getTotalElements());
        response.setPages(page.getTotalPages());
        response.setRecords(page.getContent());
        return response;
    }
}