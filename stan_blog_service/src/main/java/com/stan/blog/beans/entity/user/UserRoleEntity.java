package com.stan.blog.beans.entity.user;

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
@Table(name = "stan_blog_core_user_role")
@SQLDelete(sql = "UPDATE user_role SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class UserRoleEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String role;
    private Long userId;


    public UserRoleEntity(String role, Long userId) {
        this.role = role;
        this.userId = userId;
        this.deleted = Boolean.FALSE;
    }

    // to overwrite the super createBy that annotated by auto-filled
    private String createBy = "New Created";
}
