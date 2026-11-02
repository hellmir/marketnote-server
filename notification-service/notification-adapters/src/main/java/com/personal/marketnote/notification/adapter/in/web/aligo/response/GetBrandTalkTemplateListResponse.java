package com.personal.marketnote.notification.adapter.in.web.aligo.response;

import com.personal.marketnote.notification.port.in.result.aligo.GetBrandTalkTemplateListResult;

import java.util.List;

public record GetBrandTalkTemplateListResponse(
        PageInfoResponse pageInfo,
        List<BrandTalkTemplateResponse> data
) {

    public record PageInfoResponse(int currentPage, int pageSize, int total) {
    }

    public record BrandTalkTemplateResponse(
            String templateCode,
            String templateName,
            String templateType,
            String templateStatus,
            List<String> sendVariable,
            String createdAt
    ) {
    }

    public static GetBrandTalkTemplateListResponse from(GetBrandTalkTemplateListResult result) {
        PageInfoResponse pageInfo = new PageInfoResponse(
                result.pageInfo().currentPage(), result.pageInfo().pageSize(), result.pageInfo().total()
        );

        List<BrandTalkTemplateResponse> data = result.data().stream()
                .map(item -> new BrandTalkTemplateResponse(
                        item.templateCode(), item.templateName(), item.templateType(),
                        item.templateStatus(), item.sendVariable(), item.createdAt()
                ))
                .toList();

        return new GetBrandTalkTemplateListResponse(pageInfo, data);
    }
}
