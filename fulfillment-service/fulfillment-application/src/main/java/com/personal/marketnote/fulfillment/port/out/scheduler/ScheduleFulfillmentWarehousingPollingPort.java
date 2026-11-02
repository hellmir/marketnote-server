package com.personal.marketnote.fulfillment.port.out.scheduler;

/**
 * 풀필먼트 입고 폴링 스케줄러 포트
 *
 * @Author 성효빈
 * @Date 2026-02-09
 * @Description 풀필먼트 입고 폴링 스케줄링 기능을 제공합니다.
 */
public interface ScheduleFulfillmentWarehousingPollingPort {

    /**
     * @param command 풀필먼트 입고 폴링 스케줄링 커맨드
     * @Date 2026-02-09
     * @Author 성효빈
     * @Description 풀필먼트 입고 폴링을 스케줄링합니다.
     */
    void schedule(ScheduleFulfillmentWarehousingPollingCommand command);
}
