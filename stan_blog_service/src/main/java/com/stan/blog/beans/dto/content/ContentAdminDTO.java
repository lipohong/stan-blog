package com.stan.blog.beans.dto.content;

import lombok.Data;

@Data
public class ContentAdminDTO {
    private String contentId;
    private Boolean banned;
    private Boolean recommended;
    private String reason;
}
