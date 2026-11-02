package com.personal.marketnote.notification.port.in.result.aligo;

import java.util.List;

public record GetBrandTalkTemplateListResult(
        PageInfo pageInfo,
        List<BrandTalkTemplateItem> data
) {

    public record PageInfo(int currentPage, int pageSize, int total) {
    }

    public record BrandTalkTemplateItem(
            String templateCode,
            String templateName,
            String templateType,
            String templateStatus,
            List<String> sendVariable,
            String createdAt
    ) {
    }

    public record ExternalListResponse(
            PageInfo pageInfo,
            List<ExternalTemplateItem> templates
    ) {
    }

    public record ExternalTemplateItem(
            String templateCode,
            String templateName,
            String templateType,
            String templateStatus,
            List<String> sendVariable,
            String createdAt
    ) {
    }
}
