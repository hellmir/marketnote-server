package com.personal.marketnote.notification.adapter.out.persistence.aligo;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.notification.adapter.out.persistence.aligo.entity.AligoTemplateJpaEntity;
import com.personal.marketnote.notification.adapter.out.persistence.aligo.repository.AligoTemplateJpaRepository;
import com.personal.marketnote.notification.domain.aligo.AligoTemplate;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateButton;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateButtonLinkType;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateCreateState;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateEmType;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateInspectionStatus;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateNotFoundException;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateSnapshotState;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateStatus;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Import({AuditConfig.class, AligoTemplatePersistenceAdapter.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("AligoTemplatePersistenceAdapter 테스트")
class AligoTemplatePersistenceAdapterTest {

    @Autowired
    private AligoTemplatePersistenceAdapter adapter;

    @Autowired
    private AligoTemplateJpaRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("save 호출 시 템플릿과 버튼이 함께 저장되고 ordering 이 유지된다")
    void shouldSaveTemplateWithButtonsAndKeepOrdering() {
        // given
        List<AligoTemplateButton> buttons = List.of(
                AligoTemplateButton.builder()
                        .ordering(1)
                        .name("버튼1")
                        .linkType(AligoTemplateButtonLinkType.WL)
                        .linkMo("https://m.example.com/1")
                        .linkPc("https://example.com/1")
                        .build(),
                AligoTemplateButton.builder()
                        .ordering(2)
                        .name("버튼2")
                        .linkType(AligoTemplateButtonLinkType.AL)
                        .linkIos("ios-link")
                        .linkAnd("and-link")
                        .build(),
                AligoTemplateButton.builder()
                        .ordering(3)
                        .name("버튼3")
                        .linkType(AligoTemplateButtonLinkType.AC)
                        .build()
        );
        AligoTemplate template = AligoTemplate.from(
                AligoTemplateCreateState.builder()
                        .senderKey("sender-key-save")
                        .templateCode("TEMPLATE-SAVE-001")
                        .templateName("저장 템플릿")
                        .templateContent("본문")
                        .templateType(AligoTemplateType.BA)
                        .templateEmType(AligoTemplateEmType.NONE)
                        .buttons(buttons)
                        .build()
        );

        // when
        AligoTemplate saved = adapter.save(template);
        entityManager.flush();
        entityManager.clear();

        // then
        AligoTemplateJpaEntity entity = repository.findByTemplateCodeWithButtons("TEMPLATE-SAVE-001").orElseThrow();
        assertThat(saved.getTemplateCode()).isEqualTo("TEMPLATE-SAVE-001");
        assertThat(entity.getButtons()).hasSize(3);
        assertThat(entity.getButtons())
                .extracting("ordering")
                .containsExactly(1, 2, 3);
        assertThat(entity.getButtons())
                .extracting("name")
                .containsExactly("버튼1", "버튼2", "버튼3");
    }

    @Test
    @DisplayName("update 메서드에 @Transactional READ_COMMITTED 격리 수준이 지정되어 있다")
    void shouldDeclareReadCommittedIsolationOnUpdate() throws NoSuchMethodException {
        // given
        Method method = AligoTemplatePersistenceAdapter.class.getMethod("update", AligoTemplate.class);
        org.springframework.transaction.annotation.Transactional txAnnotation =
                method.getAnnotation(org.springframework.transaction.annotation.Transactional.class);

        // then
        assertThat(txAnnotation).isNotNull();
        assertThat(txAnnotation.isolation())
                .isEqualTo(org.springframework.transaction.annotation.Isolation.READ_COMMITTED);
    }

    @Test
    @DisplayName("update 호출 시 템플릿 내용과 버튼이 갱신된다")
    void shouldUpdateTemplateContentAndButtons() {
        // given
        AligoTemplate initial = adapter.save(buildSimpleTemplate("TEMPLATE-UPDATE-001", "sender-key-update"));
        entityManager.flush();
        entityManager.clear();

        List<AligoTemplateButton> newButtons = List.of(
                AligoTemplateButton.builder()
                        .ordering(1)
                        .name("새 버튼")
                        .linkType(AligoTemplateButtonLinkType.WL)
                        .linkMo("https://m.example.com/new")
                        .linkPc("https://example.com/new")
                        .build()
        );
        AligoTemplate updateRequest = AligoTemplate.from(
                AligoTemplateSnapshotState.builder()
                        .id(initial.getId())
                        .senderKey("sender-key-update")
                        .templateCode("TEMPLATE-UPDATE-001")
                        .templateName("수정된 이름")
                        .templateContent("수정된 본문")
                        .templateTitle("수정된 타이틀")
                        .templateSubtitle("수정된 서브타이틀")
                        .templateType(AligoTemplateType.BA)
                        .templateEmType(AligoTemplateEmType.NONE)
                        .inspStatus(AligoTemplateInspectionStatus.REQ.name())
                        .templateStatus(AligoTemplateStatus.A)
                        .comments("코멘트")
                        .buttons(newButtons)
                        .status(com.personal.marketnote.common.domain.EntityStatus.ACTIVE)
                        .build()
        );

        // when
        adapter.update(updateRequest);
        entityManager.flush();
        entityManager.clear();

        // then
        AligoTemplateJpaEntity entity = repository.findByTemplateCodeWithButtons("TEMPLATE-UPDATE-001").orElseThrow();
        assertThat(entity.getTemplateName()).isEqualTo("수정된 이름");
        assertThat(entity.getTemplateContent()).isEqualTo("수정된 본문");
        assertThat(entity.getTemplateTitle()).isEqualTo("수정된 타이틀");
        assertThat(entity.getTemplateSubtitle()).isEqualTo("수정된 서브타이틀");
        assertThat(entity.getInspStatus()).isEqualTo(AligoTemplateInspectionStatus.REQ.name());
        assertThat(entity.getTemplateStatus()).isEqualTo(AligoTemplateStatus.A);
        assertThat(entity.getComments()).isEqualTo("코멘트");
        assertThat(entity.getButtons()).hasSize(1);
        assertThat(entity.getButtons().get(0).getName()).isEqualTo("새 버튼");
    }

    @Test
    @DisplayName("update 호출 시 템플릿이 존재하지 않으면 AligoTemplateNotFoundException 이 발생한다")
    void shouldThrowWhenUpdatingNonExistentTemplate() {
        // given
        AligoTemplate notFound = AligoTemplate.from(
                AligoTemplateSnapshotState.builder()
                        .id(999L)
                        .senderKey("sender-key-missing")
                        .templateCode("TEMPLATE-MISSING-001")
                        .templateName("이름")
                        .templateContent("본문")
                        .templateType(AligoTemplateType.BA)
                        .templateEmType(AligoTemplateEmType.NONE)
                        .inspStatus(AligoTemplateInspectionStatus.REG.name())
                        .templateStatus(AligoTemplateStatus.R)
                        .buttons(List.of())
                        .status(com.personal.marketnote.common.domain.EntityStatus.ACTIVE)
                        .build()
        );

        // when & then
        assertThatThrownBy(() -> adapter.update(notFound))
                .isInstanceOf(AligoTemplateNotFoundException.class);
    }

    @Test
    @DisplayName("deleteByTemplateCode 호출 시 템플릿이 삭제되고 연관 버튼도 함께 제거된다")
    void shouldDeleteTemplateAndCascadeButtons() {
        // given
        List<AligoTemplateButton> buttons = List.of(
                AligoTemplateButton.builder()
                        .ordering(1)
                        .name("버튼A")
                        .linkType(AligoTemplateButtonLinkType.AC)
                        .build()
        );
        AligoTemplate template = AligoTemplate.from(
                AligoTemplateCreateState.builder()
                        .senderKey("sender-key-delete")
                        .templateCode("TEMPLATE-DELETE-001")
                        .templateName("삭제 템플릿")
                        .templateContent("본문")
                        .templateType(AligoTemplateType.BA)
                        .templateEmType(AligoTemplateEmType.NONE)
                        .buttons(buttons)
                        .build()
        );
        adapter.save(template);
        entityManager.flush();
        entityManager.clear();

        // when
        adapter.deleteByTemplateCode("TEMPLATE-DELETE-001");
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(repository.findByTemplateCodeWithButtons("TEMPLATE-DELETE-001")).isEmpty();
        Long buttonCount = entityManager.createQuery(
                "SELECT COUNT(b) FROM AligoTemplateButtonJpaEntity b", Long.class
        ).getSingleResult();
        assertThat(buttonCount).isZero();
    }

    @Test
    @DisplayName("findByTemplateCode 호출 시 JOIN FETCH 로 버튼이 함께 로딩된다")
    void shouldFetchButtonsEagerlyViaJoinFetch() {
        // given
        List<AligoTemplateButton> buttons = List.of(
                AligoTemplateButton.builder()
                        .ordering(1)
                        .name("버튼1")
                        .linkType(AligoTemplateButtonLinkType.AC)
                        .build(),
                AligoTemplateButton.builder()
                        .ordering(2)
                        .name("버튼2")
                        .linkType(AligoTemplateButtonLinkType.AC)
                        .build()
        );
        AligoTemplate template = AligoTemplate.from(
                AligoTemplateCreateState.builder()
                        .senderKey("sender-key-fetch")
                        .templateCode("TEMPLATE-FETCH-001")
                        .templateName("페칭 템플릿")
                        .templateContent("본문")
                        .templateType(AligoTemplateType.BA)
                        .templateEmType(AligoTemplateEmType.NONE)
                        .buttons(buttons)
                        .build()
        );
        adapter.save(template);
        entityManager.flush();
        entityManager.clear();

        // when
        Optional<AligoTemplate> result = adapter.findByTemplateCode("TEMPLATE-FETCH-001");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getButtons()).hasSize(2);
        assertThat(result.get().getButtons())
                .extracting(AligoTemplateButton::getName)
                .containsExactly("버튼1", "버튼2");
    }

    private AligoTemplate buildSimpleTemplate(String templateCode, String senderKey) {
        return AligoTemplate.from(
                AligoTemplateCreateState.builder()
                        .senderKey(senderKey)
                        .templateCode(templateCode)
                        .templateName("초기 템플릿")
                        .templateContent("초기 본문")
                        .templateType(AligoTemplateType.BA)
                        .templateEmType(AligoTemplateEmType.NONE)
                        .buttons(List.of())
                        .build()
        );
    }
}
