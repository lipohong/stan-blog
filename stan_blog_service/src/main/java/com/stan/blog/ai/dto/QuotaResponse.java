package com.stan.blog.ai.dto;

import lombok.Data;

@Data
public class QuotaResponse {
    private boolean allowed;
    private int remaining;
}