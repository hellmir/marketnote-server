package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFasstoWarehousingAbnormalImageCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoWarehousingAbnormalImageResult;

/**
 * 파스토 입고 이상 이미지 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-17
 * @Description 파스토 입고 이상 이미지 조회 기능을 제공합니다.
 */
public interface GetFasstoWarehousingAbnormalImageUseCase {
    GetFasstoWarehousingAbnormalImageResult getWarehousingAbnormalImage(GetFasstoWarehousingAbnormalImageCommand command);
}
