package com.stan.blog.beans.entity.file;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.stan.blog.beans.entity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "stan_blog_file_resource")
@SQLDelete(sql = "UPDATE stan_blog_file_resource SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class FileResourceEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
