package com.stan.blog.core.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.stan.blog.beans.consts.Const;
import com.stan.blog.beans.dto.user.UserBriefProfileDTO;
import com.stan.blog.beans.dto.user.UserCreationDTO;
import com.stan.blog.beans.dto.user.UserFeatureDTO;
import com.stan.blog.beans.dto.user.UserFullProfileDTO;
import com.stan.blog.beans.dto.user.UserGeneralDTO;
import com.stan.blog.beans.dto.user.UserUpdateDTO;
import com.stan.blog.beans.entity.user.UserEntity;
import com.stan.blog.beans.entity.user.UserFeatureEntity;
import com.stan.blog.beans.entity.user.UserRoleEntity;
import com.stan.blog.beans.repository.user.UserRepository;
import com.stan.blog.core.exception.StanBlogRuntimeException;
import com.stan.blog.core.utils.BasicConverter;
import com.stan.blog.core.utils.CacheUtil;
import com.stan.blog.core.utils.GsonUtil;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleService userRoleService;
    private final UserFeatureService userFeatureService;
    private final CacheUtil cacheUtil;
    private final ApplicationContext applicationContext;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserGeneralDTO getUser(long id) {
        return userRepository.findById(id)
                .map(this::toUserGeneralDTO)
                .orElse(null);
    }

    public UserBriefProfileDTO getUserBriefProfile(long id) {
        return userRepository.findById(id)
                .map(u -> BasicConverter.convert(u, UserBriefProfileDTO.class))
                .orElse(null);
    }

    public UserFullProfileDTO getUserFullProfile(long id) {
        UserFullProfileDTO result = userRepository.findById(id)
                .map(u -> BasicConverter.convert(u, UserFullProfileDTO.class))
                .orElse(null);
        if (Objects.isNull(result)) {
            return null;
        }
        userFeatureService.findByUserId(id)
                .map(feature -> BasicConverter.convert(feature, UserFeatureDTO.class))
                .ifPresent(result::setFeatures);
        return result;
    }

    public Page<UserGeneralDTO> getUsers(int current, int size) {
        Pageable pageable = resolvePageable(current, size);
        Page<UserEntity> userPage = userRepository.findAll(pageable);
        List<UserGeneralDTO> userDTOs = userPage.getContent().stream()
                .map(this::toUserGeneralDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(userDTOs, pageable, userPage.getTotalElements());
    }

    public Page<UserGeneralDTO> getUsers(int current, int size, String keyword, Boolean emailVerified) {
        Pageable pageable = resolvePageable(current, size);
        Specification<UserEntity> specification = buildSpecification(keyword, emailVerified);
        Page<UserEntity> userPage = userRepository.findAll(specification, pageable);
        List<UserGeneralDTO> userDTOs = userPage.getContent().stream()
                .map(this::toUserGeneralDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(userDTOs, pageable, userPage.getTotalElements());
    }

    @Transactional
    public UserGeneralDTO createUser(UserCreationDTO dto) {
        validateRequestSubmitted(dto);
        validateEmailExist(dto.getEmail());

        UserEntity userEntity = BasicConverter.convert(dto, UserEntity.class);
        userEntity.setUsername(getDefaultUsernameFromEmail(userEntity.getEmail()));
        userEntity.setPassword(encoder.encode(dto.getPassword()));
        userEntity.setDeleted(Boolean.FALSE);
        userEntity.setEmailVerified(Boolean.FALSE);

        UserEntity persisted = userRepository.save(userEntity);

        List<UserRoleEntity> defaultRoles = createDefaultRole(persisted.getId());
        userRoleService.saveAll(defaultRoles);

        userFeatureService.save(createDefaultFeature(persisted.getId()));

        try {
            EmailVerificationService emailVerificationService = applicationContext.getBean(EmailVerificationService.class);
            emailVerificationService.sendVerificationEmail(persisted.getEmail(), persisted.getFirstName());
        } catch (Exception e) {
            log.error("Failed to send verification email during user registration for email: {}", persisted.getEmail(), e);
        }

        return BasicConverter.convert(persisted, UserGeneralDTO.class);
    }

    @Transactional
    public UserGeneralDTO updateUser(UserUpdateDTO dto) {
        Optional<UserEntity> optionalEntity = userRepository.findById(dto.getId());
        if (optionalEntity.isEmpty()) {
            log.warn("user doesn't exist, id: {}", dto.getId());
            return new UserGeneralDTO();
        }
        UserEntity entity = optionalEntity.get();
        BeanUtils.copyProperties(dto, entity);
        UserEntity saved = userRepository.save(entity);

        UserGeneralDTO result = BasicConverter.convert(saved, UserGeneralDTO.class);
        UserFeatureDTO featureDTO = updateFeature(dto.getFeatures(), dto.getId());
        result.setFeatures(featureDTO);
        return result;
    }

    @Transactional
    public void deleteUser(long id) {
        userFeatureService.deleteByUserId(id);
        userRepository.deleteById(id);
    }

    @Transactional
    public UserGeneralDTO updateUserRoles(long userId, List<String> roles) {
        userRoleService.deleteByUserId(userId);
        List<UserRoleEntity> newRoles = roles.stream()
                .map(role -> new UserRoleEntity(role, userId))
                .collect(Collectors.toList());
        if (!newRoles.isEmpty()) {
            userRoleService.saveAll(newRoles);
        }
        return getUser(userId);
    }

    public boolean isEmailRegistered(String email) {
        return userRepository.existsByEmail(email);
    }

    public String getFirstNameByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserEntity::getFirstName)
                .orElse(null);
    }

    @Transactional
    public boolean updatePassword(String email, String newPassword) {
        return userRepository.findByEmail(email)
                .map(user -> {
                    user.setPassword(encoder.encode(newPassword));
                    userRepository.save(user);
                    log.info("Password updated successfully for user with email: {}", email);
                    return true;
                })
                .orElseGet(() -> {
                    log.warn("Failed to update password: user with email {} not found", email);
                    return false;
                });
    }

    public Optional<UserEntity> findByUsernameOrEmailOrPhone(String principal) {
        return userRepository.findByUsernameOrEmailOrPhoneNum(principal, principal, principal);
    }
    public Optional<UserEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public UserEntity saveUser(UserEntity entity) {
        return userRepository.save(entity);
    }


    private UserFeatureDTO updateFeature(UserFeatureDTO dto, Long userId) {
        if (dto == null) {
            return userFeatureService.findByUserId(userId)
                    .map(feature -> BasicConverter.convert(feature, UserFeatureDTO.class))
                    .orElse(null);
        }
        UserFeatureEntity featureEntity = userFeatureService.findByUserId(userId)
                .orElseGet(UserFeatureEntity::new);
        BeanUtils.copyProperties(dto, featureEntity);
        featureEntity.setUserId(userId);
        UserFeatureEntity saved = userFeatureService.saveOrUpdate(featureEntity);
        return BasicConverter.convert(saved, UserFeatureDTO.class);
    }

    private void validateRequestSubmitted(UserCreationDTO dto) {
        String payload = GsonUtil.toJson(dto);
        if (cacheUtil.getExpireTime(payload) > 0) {
            throw new StanBlogRuntimeException("The request has been submitted, please don't repeatly submit.");
        }
        cacheUtil.set(payload, null, 3);
    }

    private void validateEmailExist(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new StanBlogRuntimeException("The email has been registered, please login directly.");
        }
    }

    private Pageable resolvePageable(int current, int size) {
        int resolvedPage = Math.max(current - 1, 0);
        int resolvedSize = Math.max(size, 1);
        return PageRequest.of(resolvedPage, resolvedSize, Sort.by(Sort.Direction.DESC, "createTime"));
    }

    private Specification<UserEntity> buildSpecification(String keyword, Boolean emailVerified) {
        Specification<UserEntity> specification = Specification.where(null);
        if (StringUtils.hasText(keyword)) {
            String searchKeyword = "%" + keyword.trim().toLowerCase() + "%";
            specification = specification.and((root, query, cb) -> {
                Predicate usernamePredicate = cb.like(cb.lower(root.get("username")), searchKeyword);
                Predicate firstNamePredicate = cb.like(cb.lower(root.get("firstName")), searchKeyword);
                Predicate lastNamePredicate = cb.like(cb.lower(root.get("lastName")), searchKeyword);
                Predicate emailPredicate = cb.like(cb.lower(root.get("email")), searchKeyword);
                return cb.or(usernamePredicate, firstNamePredicate, lastNamePredicate, emailPredicate);
            });
        }
        if (emailVerified != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("emailVerified"), emailVerified));
        }
        return specification;
    }

    private UserGeneralDTO toUserGeneralDTO(UserEntity user) {
        UserGeneralDTO dto = BasicConverter.convert(user, UserGeneralDTO.class);
        List<UserRoleEntity> userRoles = userRoleService.findByUserId(user.getId());
        dto.setRoles(userRoles.stream().map(UserRoleEntity::getRole).collect(Collectors.toList()));
        userFeatureService.findByUserId(user.getId())
                .map(feature -> BasicConverter.convert(feature, UserFeatureDTO.class))
                .ifPresent(dto::setFeatures);
        return dto;
    }

    private List<UserRoleEntity> createDefaultRole(Long userId) {
        return List.of(new UserRoleEntity(Const.Role.BASIC.getValue(), userId));
    }

    private UserFeatureEntity createDefaultFeature(Long userId) {
        UserFeatureEntity feature = new UserFeatureEntity();
        feature.setUserId(userId);
        feature.setArticleModule(Boolean.TRUE);
        feature.setPlanModule(Boolean.TRUE);
        feature.setVocabularyModule(Boolean.TRUE);
        return feature;
    }

    private String getDefaultUsernameFromEmail(String email) {
        int atIndex = email.indexOf('@');
        return atIndex > 0 ? email.substring(0, atIndex) : email;
    }
}


