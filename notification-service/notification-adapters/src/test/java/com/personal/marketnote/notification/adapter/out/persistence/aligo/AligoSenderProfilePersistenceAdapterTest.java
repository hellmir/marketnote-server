package com.personal.marketnote.notification.adapter.out.persistence.aligo;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.adapter.out.persistence.aligo.entity.AligoSenderProfileJpaEntity;
import com.personal.marketnote.notification.adapter.out.persistence.aligo.repository.AligoSenderProfileJpaRepository;
import com.personal.marketnote.notification.domain.aligo.AligoProfileInspectionStatus;
import com.personal.marketnote.notification.domain.aligo.AligoSenderProfile;
import com.personal.marketnote.notification.domain.aligo.AligoSenderProfileCreateState;
import com.personal.marketnote.notification.domain.aligo.AligoSenderProfileNotFoundException;
import com.personal.marketnote.notification.domain.aligo.AligoSenderProfileSnapshotState;
import com.personal.marketnote.notification.domain.aligo.AligoSenderProfileStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Import({AuditConfig.class, AligoSenderProfilePersistenceAdapter.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("AligoSenderProfilePersistenceAdapter 테스트")
class AligoSenderProfilePersistenceAdapterTest {

    @Autowired
    private AligoSenderProfilePersistenceAdapter adapter;

    @Autowired
    private AligoSenderProfileJpaRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("save 호출 시 엔티티가 저장되고 mapper 결과 Optional 에서 도메인으로 언래핑된다")
    void shouldSaveAndUnwrapMappedDomain() {
        // given
        AligoSenderProfile profile = buildProfile("sender-key-save", "@plus-save");

        // when
        AligoSenderProfile saved = adapter.save(profile);
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getSenderKey()).isEqualTo("sender-key-save");
        assertThat(saved.getPlusId()).isEqualTo("@plus-save");
        assertThat(repository.findBySenderKey("sender-key-save")).isPresent();
    }

    @Test
    @DisplayName("findBySenderKey 는 존재하는 엔티티를 Optional 로 반환한다")
    void shouldFindBySenderKey() {
        // given
        adapter.save(buildProfile("sender-key-find", "@plus-find"));
        entityManager.flush();
        entityManager.clear();

        // when
        var result = adapter.findBySenderKey("sender-key-find");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getSenderKey()).isEqualTo("sender-key-find");
        assertThat(result.get().getPlusId()).isEqualTo("@plus-find");
    }

    @Test
    @DisplayName("findBySenderKey 는 존재하지 않는 senderKey 에 대해 빈 Optional 을 반환한다")
    void shouldReturnEmptyWhenSenderKeyNotFound() {
        // when
        var result = adapter.findBySenderKey("missing-sender-key");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAll 은 stream + flatMap + Optional 필터로 유효한 도메인만 반환한다")
    void shouldReturnAllProfilesViaStreamOptionalFilter() {
        // given
        adapter.save(buildProfile("sender-key-all-1", "@plus-1"));
        adapter.save(buildProfile("sender-key-all-2", "@plus-2"));
        adapter.save(buildProfile("sender-key-all-3", "@plus-3"));
        entityManager.flush();
        entityManager.clear();

        // when
        List<AligoSenderProfile> result = adapter.findAll();

        // then
        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(AligoSenderProfile::getSenderKey)
                .containsExactlyInAnyOrder("sender-key-all-1", "sender-key-all-2", "sender-key-all-3");
        assertThat(result).allSatisfy(p -> assertThat(p.getId()).isNotNull());
    }

    @Test
    @DisplayName("update 호출 시 엔티티의 updateFrom 이 호출되어 검수상태/프로필상태/카테고리/이름이 갱신된다")
    void shouldCallUpdateFromOnEntity() {
        // given
        AligoSenderProfile initial = adapter.save(buildProfile("sender-key-update", "@plus-update"));
        entityManager.flush();
        entityManager.clear();

        AligoSenderProfile updated = AligoSenderProfile.from(
                AligoSenderProfileSnapshotState.builder()
                        .id(initial.getId())
                        .senderKey("sender-key-update")
                        .plusId("@plus-update")
                        .phoneNumber(initial.getPhoneNumber())
                        .categoryCode("NEW-CATEGORY")
                        .name("새 이름")
                        .licenseNum(initial.getLicenseNum())
                        .profileInspectionStatus(AligoProfileInspectionStatus.B)
                        .senderProfileStatus(AligoSenderProfileStatus.S)
                        .alimUseYn(false)
                        .uuid(initial.getUuid())
                        .status(EntityStatus.ACTIVE)
                        .build()
        );

        // when
        adapter.update(updated);
        entityManager.flush();
        entityManager.clear();

        // then
        AligoSenderProfileJpaEntity entity = repository.findBySenderKey("sender-key-update").orElseThrow();
        assertThat(entity.getProfileInspectionStatus()).isEqualTo(AligoProfileInspectionStatus.B);
        assertThat(entity.getSenderProfileStatus()).isEqualTo(AligoSenderProfileStatus.S);
        assertThat(entity.isAlimUseYn()).isFalse();
        assertThat(entity.getCategoryCode()).isEqualTo("NEW-CATEGORY");
        assertThat(entity.getName()).isEqualTo("새 이름");
        assertThat(entity.getPlusId()).isEqualTo("@plus-update");
    }

    @Test
    @DisplayName("update 호출 시 프로필이 존재하지 않으면 AligoSenderProfileNotFoundException 이 발생한다")
    void shouldThrowWhenUpdatingNonExistentProfile() {
        // given
        AligoSenderProfile missing = AligoSenderProfile.from(
                AligoSenderProfileSnapshotState.builder()
                        .id(999L)
                        .senderKey("sender-key-missing")
                        .plusId("@plus-missing")
                        .profileInspectionStatus(AligoProfileInspectionStatus.A)
                        .senderProfileStatus(AligoSenderProfileStatus.A)
                        .alimUseYn(true)
                        .status(EntityStatus.ACTIVE)
                        .build()
        );

        // when & then
        assertThatThrownBy(() -> adapter.update(missing))
                .isInstanceOf(AligoSenderProfileNotFoundException.class);
    }

    private AligoSenderProfile buildProfile(String senderKey, String plusId) {
        return AligoSenderProfile.from(
                AligoSenderProfileCreateState.builder()
                        .senderKey(senderKey)
                        .plusId(plusId)
                        .phoneNumber("01011112222")
                        .categoryCode("CATEGORY-A")
                        .name("프로필 이름")
                        .licenseNum("LIC-1234")
                        .profileInspectionStatus(AligoProfileInspectionStatus.A)
                        .senderProfileStatus(AligoSenderProfileStatus.A)
                        .alimUseYn(true)
                        .uuid("uuid-" + senderKey)
                        .build()
        );
    }
}
