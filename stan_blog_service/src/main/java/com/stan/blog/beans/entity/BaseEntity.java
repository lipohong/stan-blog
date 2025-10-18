package com.stan.blog.beans.entity;

import java.io.Serializable;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Data;

@Data
@MappedSuperclass
public class BaseEntity implements Serializable {
    @Column(name = "deleted")
    protected Boolean deleted = false;
   
    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = new Timestamp(System.currentTimeMillis());
        }
        if (createBy == null) {
            createBy = "system";
        }
        // Ensure updateTime is initialized on creation for tests relying on it
        if (updateTime == null) {
            updateTime = createTime;
        }
        if (updateBy == null) {
            updateBy = "system";
        }
    }
    protected Timestamp createTime;
    protected String createBy;

    @PreUpdate
    public void preUpdate() {
        updateTime = new Timestamp(System.currentTimeMillis());
        if (updateBy == null) {
            updateBy = "system";
        }
    }
    protected Timestamp updateTime;
    protected String updateBy;
}
