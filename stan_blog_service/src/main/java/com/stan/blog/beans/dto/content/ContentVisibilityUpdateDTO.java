package com.stan.blog.beans.dto.content;

import com.stan.blog.beans.consts.Const.Visibility;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContentVisibilityUpdateDTO {
    private String id;
    private Visibility visibility;
}
