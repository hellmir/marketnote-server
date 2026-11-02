package com.personal.marketnote.reward.port.out.offerwall;

import com.personal.marketnote.reward.domain.offerwall.OfferwallMapper;

/**
 * 오퍼월 매퍼 저장 포트
 *
 * @Author 성효빈
 * @Date 2026-01-16
 * @Description 오퍼월 매퍼 저장 기능을 제공합니다.
 */
public interface SaveOfferwallMapperPort {
    /**
     * @param offerwallMapper 오퍼월 매퍼
     * @return 저장된 오퍼월 매퍼 {@link OfferwallMapper}
     * @Date 2026-01-16
     * @Author 성효빈
     * @Description 오퍼월 매퍼를 저장합니다.
     */
    OfferwallMapper save(OfferwallMapper offerwallMapper);
}
