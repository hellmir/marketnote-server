package com.personal.marketnote.product.adapter.out.persistence.servicecommunication;

import com.personal.marketnote.product.adapter.out.persistence.servicecommunication.entity.ProductServiceCommunicationHistoryJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.servicecommunication.repository.ProductServiceCommunicationHistoryJpaRepository;
import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationHistory;
import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationHistoryCreateState;
import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationSenderType;
import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationTargetType;
import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceCommunicationHistoryPersistenceAdapterTest {

    @InjectMocks
    private ProductServiceCommunicationHistoryPersistenceAdapter adapter;

    @Mock
    private ProductServiceCommunicationHistoryJpaRepository repository;

    @Test
    @DisplayName("서비스 통신 이력을 저장하면 저장된 도메인 객체를 반환한다")
    void savesAndReturnsDomain() {
        ProductServiceCommunicationHistory history = buildHistory();
        ProductServiceCommunicationHistoryJpaEntity entity = ProductServiceCommunicationHistoryJpaEntity.from(history);
        ReflectionTestUtils.setField(entity, "id", 42L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.now());
        when(repository.save(any(ProductServiceCommunicationHistoryJpaEntity.class))).thenReturn(entity);

        ProductServiceCommunicationHistory result = adapter.save(history);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(42L);
        assertThat(result.getTargetType()).isEqualTo(ProductServiceCommunicationTargetType.INVENTORY);
        assertThat(result.getCommunicationType()).isEqualTo(ProductServiceCommunicationType.REQUEST);
    }

    @Test
    @DisplayName("전달된 도메인의 필드를 엔티티로 올바르게 변환한다")
    void mapsDomainFieldsToEntity() {
        ProductServiceCommunicationHistory history = buildHistory();
        ProductServiceCommunicationHistoryJpaEntity savedEntity = ProductServiceCommunicationHistoryJpaEntity.from(history);
        ReflectionTestUtils.setField(savedEntity, "id", 1L);
        when(repository.save(any(ProductServiceCommunicationHistoryJpaEntity.class))).thenReturn(savedEntity);

        adapter.save(history);

        ArgumentCaptor<ProductServiceCommunicationHistoryJpaEntity> captor =
                ArgumentCaptor.forClass(ProductServiceCommunicationHistoryJpaEntity.class);
        verify(repository).save(captor.capture());
        ProductServiceCommunicationHistoryJpaEntity captured = captor.getValue();
        assertThat(captured.getTargetType()).isEqualTo(ProductServiceCommunicationTargetType.INVENTORY);
        assertThat(captured.getTargetId()).isEqualTo("inv-1");
        assertThat(captured.getCommunicationType()).isEqualTo(ProductServiceCommunicationType.REQUEST);
        assertThat(captured.getSender()).isEqualTo(ProductServiceCommunicationSenderType.PRODUCT);
        assertThat(captured.getException()).isEqualTo("SocketTimeout");
        assertThat(captured.getPayload()).isEqualTo("{\"k\":\"v\"}");
    }

    private ProductServiceCommunicationHistory buildHistory() {
        return ProductServiceCommunicationHistory.from(
                ProductServiceCommunicationHistoryCreateState.builder()
                        .targetType(ProductServiceCommunicationTargetType.INVENTORY)
                        .targetId("inv-1")
                        .communicationType(ProductServiceCommunicationType.REQUEST)
                        .sender(ProductServiceCommunicationSenderType.PRODUCT)
                        .exception("SocketTimeout")
                        .payload("{\"k\":\"v\"}")
                        .payloadJson(null)
                        .build()
        );
    }
}
