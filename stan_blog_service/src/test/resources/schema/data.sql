-- Init one default user profile for testing
INSERT INTO stan_blog_core_user(ID, USERNAME, PASSWORD, PHONE_NUM, EMAIL, LAST_NAME, FIRST_NAME, ADDRESS, AVATAR_URL, INTRODUCTION, BLOG, BACKGROUND_IMG, PHOTO_IMG, PROFESSION, DELETED, EMAIL_VERIFIED)
VALUES(1, 'stan_blog_admin', '$2a$10$Ldf4Rz0P13vPoS9s54hq5eOy031zz1/Jh9beysfaK7Yo59IIkcJjy', '12312341234', 'example@gmail.com', 'LI', 'Admin', 'Hong Kong', 'https://avatar.com', 'Hello world', 'https://blog.com', 'https://bg.com', 'https://photo.com', 'Developer', FALSE, FALSE);
INSERT INTO stan_blog_core_user_role(ID, ROLE, USER_ID, DELETED) VALUES(1, 'ROLE_BASIC', 1, FALSE);
INSERT INTO stan_blog_core_user_role(ID, ROLE, USER_ID, DELETED) VALUES(2, 'ROLE_ADMIN', 1, FALSE);

INSERT INTO stan_blog_core_user_feature(USER_ID, ARTICLE_MODULE, PLAN_MODULE, VOCABULARY_MODULE, COLLECTION_MODULE, DELETED)
VALUES (1, TRUE, TRUE, FALSE, FALSE, FALSE);

COMMIT;