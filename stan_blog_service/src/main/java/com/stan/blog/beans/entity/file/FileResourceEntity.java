package com.stan.blog.beans.entity.file;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stan.blog.beans.entity.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("stan_blog_file_resource")
public class FileResourceEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String originalFilename;
    private String storedFilename;
    private String storagePath; // absolute or relative path on disk
    private Long sizeInBytes;
    private String contentType;
    private Long ownerId;
    private Boolean publicToAll = Boolean.FALSE;
    private String checksum; // optional SHA-256 or MD5
}

