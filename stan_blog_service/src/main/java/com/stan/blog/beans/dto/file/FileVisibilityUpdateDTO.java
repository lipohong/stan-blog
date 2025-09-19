package com.stan.blog.beans.dto.file;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FileVisibilityUpdateDTO {
    @NotNull
    private Boolean publicToAll;
}

