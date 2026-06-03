package com.personal.marketnote.user.adapter.out.mapper;

import com.personal.marketnote.user.adapter.out.persistence.authentication.entity.RoleJpaEntity;
import com.personal.marketnote.user.adapter.out.persistence.user.entity.TermsJpaEntity;
import com.personal.marketnote.user.adapter.out.persistence.user.entity.UserJpaEntity;
import com.personal.marketnote.user.adapter.out.persistence.user.entity.UserOauth2VendorJpaEntity;
import com.personal.marketnote.user.adapter.out.persistence.user.entity.UserTermsJpaEntity;
import com.personal.marketnote.user.domain.authentication.Role;
import com.personal.marketnote.user.domain.user.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserJpaEntityToDomainMapper {
    public static Optional<User> mapToDomain(UserJpaEntity userJpaEntity) {
        return Optional.ofNullable(userJpaEntity)
                .map(entity -> {
                    Role role = mapToRoleDomain(entity.getRoleJpaEntity()).orElse(null);
                    List<UserTerms> userTerms = mapToUserTermsDomain(entity.getUserTermsJpaEntities()).orElse(List.of());
                    List<UserAuthProvider> vendors = mapToAuthProviderDomainWithoutUser(entity.getUserOauth2VendorsJpaEntities()).orElse(List.of());

                    User user = User.from(
                            UserSnapshotState.builder()
                                    .id(entity.getId())
                                    .userKey(entity.getUserKey())
                                    .nickname(entity.getNickname())
                                    .email(entity.getEmail())
                                    .password(entity.getPassword())
                                    .fullName(entity.getFullName())
                                    .phoneNumber(entity.getPhoneNumber())
                                    .referenceCode(entity.getReferenceCode())
                                    .referredUserCode(entity.getReferredUserCode())
                                    .role(role)
                                    .userAuthProviders(vendors)
                                    .userTerms(userTerms)
                                    .signedUpAt(entity.getSignedUpAt())
                                    .lastLoggedInAt(entity.getLastLoggedInAt())
                                    .status(entity.getStatus())
                                    .withdrawalYn(entity.getWithdrawalYn())
                                    .orderNum(entity.getOrderNum())
                                    .build()
                    );

                    vendors.forEach(v -> v.addUser(user));

                    return user;
                });
    }

    private static Optional<Role> mapToRoleDomain(RoleJpaEntity roleJpaEntity) {
        return Optional.ofNullable(roleJpaEntity)
                .filter(Objects::nonNull)
                .map(entity -> Role.of(entity.getId(), entity.getName()));
    }

    private static Optional<List<UserAuthProvider>> mapToAuthProviderDomainWithoutUser(List<UserOauth2VendorJpaEntity> userOauth2VendorsJpaEntities) {
        return Optional.ofNullable(userOauth2VendorsJpaEntities)
                .filter(Objects::nonNull)
                .map(entities -> entities.stream()
                        .map(entity -> UserAuthProvider.of(entity.getAuthVendor(), entity.getOidcId()))
                        .collect(Collectors.toList()));
    }

    private static Optional<List<UserTerms>> mapToUserTermsDomain(List<UserTermsJpaEntity> userTermsJpaEntities) {
        return Optional.ofNullable(userTermsJpaEntities)
                .filter(Objects::nonNull)
                .map(entities -> entities.stream()
                        .map(entity -> UserTerms.from(
                                UserTermsSnapshotState.builder()
                                        .terms(
                                                Terms.from(
                                                        TermsSnapshotState.builder()
                                                                .id(entity.getTermsJpaEntity().getId())
                                                                .content(entity.getTermsJpaEntity().getContent())
                                                                .requiredYn(entity.getTermsJpaEntity().getRequiredYn())
                                                                .createdAt(entity.getCreatedAt())
                                                                .modifiedAt(entity.getModifiedAt())
                                                                .status(entity.getStatus())
                                                                .build()
                                                )
                                        )
                                        .agreementYn(entity.getAgreementYn())
                                        .createdAt(entity.getCreatedAt())
                                        .modifiedAt(entity.getModifiedAt())
                                        .build()
                        ))
                        .collect(Collectors.toList()));
    }

    public static Optional<Terms> mapToTermsDomain(TermsJpaEntity termsJpaEntity) {
        return Optional.ofNullable(termsJpaEntity)
                .filter(Objects::nonNull)
                .map(entity -> Terms.from(
                        TermsSnapshotState.builder()
                                .id(entity.getId())
                                .content(entity.getContent())
                                .requiredYn(entity.getRequiredYn())
                                .createdAt(entity.getCreatedAt())
                                .modifiedAt(entity.getModifiedAt())
                                .status(entity.getStatus())
                                .build()
                ));
    }
}
