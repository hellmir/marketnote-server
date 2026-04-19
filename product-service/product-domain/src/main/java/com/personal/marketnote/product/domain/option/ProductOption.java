package com.personal.marketnote.product.domain.option;

import com.personal.marketnote.common.domain.EntityStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ProductOption {
    private Long id;
    private ProductOptionCategory category;
    private String content;
    private EntityStatus status;

    public static ProductOption from(ProductOptionCreateState state) {
        return ProductOption.builder()
                .category(state.getCategory())
                .content(state.getContent())
                .status(EntityStatus.ACTIVE)
                .build();
    }

    public static ProductOption from(ProductOptionSnapshotState state) {
        return ProductOption.builder()
                .id(state.getId())
                .category(state.getCategory())
                .content(state.getContent())
                .status(state.getStatus())
                .build();
    }

    public void addContent(Set<Long> ids, Set<String> contents) {
        if (ids.contains(id)) {
            contents.add(content);
        }
    }
}
