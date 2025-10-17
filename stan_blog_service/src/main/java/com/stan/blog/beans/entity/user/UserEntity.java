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
@Table(name = "stan_blog_core_user")
@SQLDelete(sql = "UPDATE stan_blog_core_user SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class UserEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String lastName;
    private String firstName;
    private String phoneNum;
    private String email;
    private String address;
    private String username;
    private String password;
    private String avatarUrl;
    private String introduction;
    private String blog;
    // to overwrite the super createBy that annotated by auto-filled
    private String createBy = "New Created";
    private String photoImg;
    private String backgroundImg;
    private String profession;
    private Boolean emailVerified = Boolean.FALSE;
}
